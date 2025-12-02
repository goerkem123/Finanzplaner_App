package com.example.finanzplaner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    // UI-Elemente
    private ImageButton btnLogout;
    private ImageButton btnCategories;
    private FloatingActionButton fabAdd;
    private BudgetAdapter budgetAdapter;
    private TextView tvBalance, tvIncome, tvExpense, tvOverviewTitle;
    private androidx.recyclerview.widget.RecyclerView recyclerViewBudgets;
    private java.util.List<Category> categoryList;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Firebase initialisieren
        mAuth = FirebaseAuth.getInstance();

        // Views verbinden
        btnLogout = findViewById(R.id.btn_logout);
        btnCategories = findViewById(R.id.btn_categories);
        fabAdd = findViewById(R.id.fab_add_transaction);

        // DIE IDS AUS DEM XML-LAYOUT
        tvBalance = findViewById(R.id.tv_balance);        // Gesamtsaldo
        tvIncome = findViewById(R.id.tv_income_amount);   // Einnahmen
        tvExpense = findViewById(R.id.tv_expense_amount); // Ausgaben
        tvOverviewTitle = findViewById(R.id.tv_overview_title); //Tittel

        // RecyclerView einrichten
        recyclerViewBudgets = findViewById(R.id.recycler_view_budgets);
        // Liste untereinander angezeigen
        recyclerViewBudgets.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        // Leere Liste erstellen und Adapter verbinden
        categoryList = new java.util.ArrayList<>();
        budgetAdapter = new BudgetAdapter(categoryList);
        recyclerViewBudgets.setAdapter(budgetAdapter);

        // Logout-Button Logik (erweitert um Firebase Logout)
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut(); // Nutzer bei Firebase ausloggen
            Intent i = new Intent(HomeActivity.this, LoginActivity.class);
            // Diese Flags löschen die gesamte Historie (Back Stack)
            // Damit kann man nicht mehr "Zurück" in die App gehen.
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
        //Category-Button Logik
        btnCategories.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ManageCategoriesActivity.class);
            startActivity(intent);
        });

        // Hinzufügen-Button Logik
        fabAdd.setOnClickListener(v -> {
            Intent i = new Intent(this, AddTransactionActivity.class);
            startActivity(i);
        });
        //Namen laden und setzen
        loadUserName();
        // Navigation Logik
        setupBottomNavigation();
    }

    // Diese Methode wird immer aufgerufen, wenn der Bildschirm (wieder) sichtbar wird.
    // Perfekt, um die Daten neu zu laden, wenn man z.B. vom "Hinzufügen"-Screen zurückkommt.
    @Override
    protected void onResume() {
        super.onResume();
        loadFinancialData();
    }
    // Standard-Kategorien erstellen
    private void checkAndCreateDefaultCategories() {
        if (mAuth.getCurrentUser() == null) return;

        // Wir nutzen einfach getCategories um zu prüfen, ob es welche gibt
        FirestoreManager.getInstance().getCategories(new FirestoreCallback<List<Category>>() {
            @Override
            public void onCallback(List<Category> result) {
                if (result.isEmpty()) {
                    createCategory("Lebensmittel");
                    createCategory("Miete");
                    createCategory("Gehalt");
                }
            }
            @Override
            public void onFailure(Exception e) { /* Egal */ }
        });
    }
    // Hilfsmethode zum Speichern einer Kategorie
    private void createCategory(String name) {
        if (mAuth.getCurrentUser() == null) return;
        Category cat = new Category(mAuth.getCurrentUser().getUid(), name, 0, 0);

        FirestoreManager.getInstance().addCategory(cat, new FirestoreCallback<Void>() {
            @Override
            public void onCallback(Void result) {} // Erfolg, nix tun
            @Override
            public void onFailure(Exception e) {}
        });
    }
    private void loadUserName() {
        FirestoreManager.getInstance().getUserName(new FirestoreCallback<String>() {
            @Override
            public void onCallback(String name) {
                // Wir setzen den Text: "Willkommen, Alex"
                tvOverviewTitle.setText("Willkommen, " + name);
            }

            @Override
            public void onFailure(Exception e) {
                // Falls was schief geht, lassen wir "Übersicht" oder setzen einen Standard
                tvOverviewTitle.setText("Willkommen");
            }
        });
    }

    // Hauptlogik: Daten aus Firestore laden und berechnen
    private void loadFinancialData() {
        if (mAuth.getCurrentUser() == null) return;
        tvBalance.setText("Lädt...");

        // 1. Kategorien laden
        FirestoreManager.getInstance().getCategories(new FirestoreCallback<List<Category>>() {
            @Override
            public void onCallback(List<Category> categories) {
                // Reset der aktuellen Werte
                for(Category c : categories) c.setCurrent(0);

                // 2. Transaktionen laden (verschachtelt)
                loadTransactionsAndCalculate(categories);
            }

            @Override
            public void onFailure(Exception e) {
                tvBalance.setText("Fehler");
            }
        });

    }

    private void loadTransactionsAndCalculate(List<Category> categories) {
        FirestoreManager.getInstance().getTransactions(new FirestoreCallback<List<Transaction>>() {
            @Override
            public void onCallback(List<Transaction> transactions) {
                calculateAndShowData(categories, transactions);
            }
            @Override
            public void onFailure(Exception e) {
                tvBalance.setText("Fehler");
            }
        });
    }

    private void calculateAndShowData(java.util.List<Category> categories, java.util.List<Transaction> transactions) {
        double totalIncome = 0;
        double totalExpense = 0;

        // Map für Ausgaben pro Kategorie-Name zu zählen
        java.util.Map<String, Double> categorySpendingMap = new java.util.HashMap<>();

        // Alle Transaktionen durchgehen und Summen bilden
        for (Transaction t : transactions) {
            if ("einnahme".equals(t.getType())) {
                totalIncome += t.getAmount();
            } else if ("ausgabe".equals(t.getType())) {
                totalExpense += t.getAmount();

                // Ausgaben der jeweiligen Kategorie zuordnen
                String catName = t.getCategory();
                if (catName != null) {
                    double currentSum = 0;
                    if (categorySpendingMap.containsKey(catName)) {
                        currentSum = categorySpendingMap.get(catName);
                    }
                    categorySpendingMap.put(catName, currentSum + t.getAmount());
                }
            }
        }

        // Die berechneten Ausgaben in die Kategorie-Objekte schreiben
        for (Category cat : categories) {
            if (categorySpendingMap.containsKey(cat.getName())) {
                cat.setCurrent(categorySpendingMap.get(cat.getName()));
            }
        }

        // UI Updates
        // Die oberen Karten (Bilanz)
        double balance = totalIncome - totalExpense;
        updateUI(balance, totalIncome, totalExpense);

        // Die Budget-Liste unten
        categoryList.clear();

        // Budget-Liste unten filtern
        for (Category cat : categories) {
            if (cat.getLimit() > 0 || cat.getCurrent() > 0) {
                categoryList.add(cat);
            }
        }
        budgetAdapter.notifyDataSetChanged(); // Adapter Bescheid geben
    }

    // Hilfsmethode, um die berechneten Zahlen schön formatiert anzuzeigen
    private void updateUI(double balance, double income, double expense) {
        // Zahlen als Währung formatieren (z.B. "1.234,56 €")
        // Verwendet Locale.GERMANY für das Komma statt Punkt und das €-Zeichen
        String balanceStr = String.format(Locale.GERMANY, "%.2f €", balance);
        String incomeStr = String.format(Locale.GERMANY, "%.2f €", income);
        String expenseStr = String.format(Locale.GERMANY, "%.2f €", expense);

        // Texte in die Felder setzen
        tvBalance.setText(balanceStr);
        tvIncome.setText(incomeStr);
        tvExpense.setText(expenseStr);

        // Farbe des Kontostands anpassen (Grün bei Plus, Rot bei Minus)
        // Ich habe die Farben aus dem XML deines Partners genommen.
        if (balance >= 0) {
            // Ein schönes Grün für Plus (#79A07A)
            tvBalance.setTextColor(Color.parseColor("#79A07A"));
        } else {
            // Ein Rot für Minus (#D74848) - passt zur Ausgaben-Karte
            tvBalance.setTextColor(Color.parseColor("#D74848"));
        }
    }

    // Deine alte Navigations-Logik (ausgelagert für Übersichtlichkeit)
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_transactions) {
                Intent intent = new Intent(this, TransactionsActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_diagramme) {
                startActivity(new Intent(this, DiagrammeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(this, ReportsActivity.class));
                finish();
                return true;
            }
            return false;
        });
        checkAndCreateDefaultCategories();
    }
}