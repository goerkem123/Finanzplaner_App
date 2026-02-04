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

import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
// *** Das Haupt-Dashboard der App ***
// Zeigt die Finanzübersicht (Saldo, Einnahmen, Ausgaben)
public class HomeActivity extends AppCompatActivity {

    // UI-Elemente
    private ImageButton btnLogout;
    private ImageButton btnCategories;
    private FloatingActionButton fabAdd;
    private BudgetAdapter budgetAdapter;
    private TextView tvBalance, tvIncome, tvExpense, tvOverviewTitle;
    private androidx.recyclerview.widget.RecyclerView recyclerViewBudgets;
    private List<Category> categoryList;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Firebase initialisieren
        mAuth = FirebaseAuth.getInstance();

        // Verknüpft die Java-Variablen mit den XML-Elementen (Views verbinden)
        btnLogout = findViewById(R.id.btn_logout);
        btnCategories = findViewById(R.id.btn_categories);
        fabAdd = findViewById(R.id.fab_add_transaction);
        tvBalance = findViewById(R.id.tv_balance);
        tvIncome = findViewById(R.id.tv_income_amount);
        tvExpense = findViewById(R.id.tv_expense_amount);
        tvOverviewTitle = findViewById(R.id.tv_overview_title);

        // RecyclerView einrichten
        recyclerViewBudgets = findViewById(R.id.recycler_view_budgets);
        // Liste untereinander angezeigen
        recyclerViewBudgets.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        // Leere Liste erstellen und Adapter verbinden
        categoryList = new ArrayList<>();
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

        // Prüfung auf fällige Daueraufträge (Automatisierung)
        FirestoreManager.getInstance().checkRecurringTransactions(this, new FirestoreCallback<Integer>() {
            @Override
            public void onCallback(Integer count) {
                if (count > 0) {
                    // Nur wenn wirklich was gebucht wurde, zeigen wir Toast und laden neu
                    Toast.makeText(HomeActivity.this, count + " Abos wurden automatisch gebucht!", Toast.LENGTH_LONG).show();
                    loadFinancialData(); // Daten neu laden, damit die neuen Buchungen sichtbar sind
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Fehler ignorieren wir hier meistens stillschweigend
            }
        });
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
    // Lädt den Benutzernamen für die persönliche Begrüßung ("Willkommen, Alex!")
    private void loadUserName() {
        FirestoreManager.getInstance().getUserName(new FirestoreCallback<String>() {
            @Override
            public void onCallback(String name) {
                // Wir setzen den Text: "Willkommen, Alex"
                tvOverviewTitle.setText("Willkommen, " + name + "!");
            }

            @Override
            public void onFailure(Exception e) {
                // Falls was schief geht, lassen wir "Übersicht" oder setzen einen Standard
                tvOverviewTitle.setText("Willkommen");
            }
        });
    }

    // Hauptlogik: Daten aus Firestore laden und berechnen (erst Kategorien, dann Transaktionen)
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
    // Transaktionen laden
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
    // Berechnet Summen und ordnet Ausgaben den Kategorien zu
    private void calculateAndShowData(List<Category> categories, List<Transaction> transactions) {
        double totalIncome = 0;
        double totalExpense = 0;

        // Map für Ausgaben pro Kategorie-Name zu zählen
       Map<String, Double> categorySpendingMap = new HashMap<>();

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
        String balanceStr = String.format(Locale.GERMANY, "%.2f €", balance);
        String incomeStr = String.format(Locale.GERMANY, "%.2f €", income);
        String expenseStr = String.format(Locale.GERMANY, "%.2f €", expense);

        // Texte in die Felder setzen
        tvBalance.setText(balanceStr);
        tvIncome.setText(incomeStr);
        tvExpense.setText(expenseStr);

        // Farbe des Kontostands anpassen (Grün bei Plus, Rot bei Minus)
        if (balance >= 0) {
            // Ein schönes Grün für Plus (#79A07A)
            tvBalance.setTextColor(Color.parseColor("#79A07A"));
        } else {
            // Ein Rot für Minus (#D74848) - passt zur Ausgaben-Karte
            tvBalance.setTextColor(Color.parseColor("#D74848"));
        }
    }

    // Konfiguration der unteren Navigationsleiste
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