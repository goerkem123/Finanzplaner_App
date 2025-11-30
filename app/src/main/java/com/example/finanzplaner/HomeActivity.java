package com.example.finanzplaner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    // UI-Elemente
    private ImageButton btnLogout;
    private ImageButton btnCategories;
    private FloatingActionButton fabAdd;
    private BudgetAdapter budgetAdapter;
    private TextView tvBalance, tvIncome, tvExpense;
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
    private void createCategory(String userId, String name, double limit) {
        if (mAuth.getCurrentUser() == null) return;
        Category cat = new Category(mAuth.getCurrentUser().getUid(), name, 0, 0);

        FirestoreManager.getInstance().addCategory(cat, new FirestoreCallback<Void>() {
            @Override
            public void onCallback(Void result) {} // Erfolg, nix tun
            @Override
            public void onFailure(Exception e) {}
        });
    }

    // Hauptlogik: Daten aus Firestore laden und berechnen
    private void loadFinancialData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // Sollte nicht passieren, aber sicher ist sicher: Zurück zum Login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Zeige dem Nutzer, dass geladen wird (optional)
        tvBalance.setText("Lädt...");

        //Erst holen wir alle Kategorien des Nutzers
        db.collection("categories")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(categorySnapshots -> {
                    // Wir speichern die Kategorien erstmal in einer temporären Liste
                    java.util.List<Category> loadedCategories = new java.util.ArrayList<>();

                    for (DocumentSnapshot doc : categorySnapshots) {
                        Category cat = doc.toObject(Category.class);
                        if(cat != null) {
                            cat.setId(doc.getId()); // Wichtig: ID setzen
                            cat.setCurrent(0);      // Reset: Ausgaben auf 0 setzen
                            loadedCategories.add(cat);
                        }
                    }
                    // Hier geht es gleich weiter mit den Transaktionen...
                    loadTransactions(loadedCategories); // Diese Methode erstellen wir im nächsten Schritt!
                })
                .addOnFailureListener(e -> {
                    tvBalance.setText("Fehler");
                    Toast.makeText(HomeActivity.this, "Kategorien konnten nicht geladen werden" + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void loadTransactions(java.util.List<Category> loadedCategories) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // SCHRITT 2: Jetzt holen wir die Transaktionen
        db.collection("transactions")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(transactionSnapshots -> {

                    // Liste für Transaktionen vorbereiten
                    java.util.List<Transaction> loadedTransactions = new java.util.ArrayList<>();

                    for (DocumentSnapshot doc : transactionSnapshots) {
                        Transaction t = doc.toObject(Transaction.class);
                        if (t != null) {
                            loadedTransactions.add(t);
                        }
                    }

                    // Weiter zur Berechnung...
                    calculateAndShowData(loadedCategories, loadedTransactions); // Nächster Schritt!

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "Transaktionen konnten nicht geladen werden", Toast.LENGTH_SHORT).show();
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