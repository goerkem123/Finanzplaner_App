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

import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    // UI-Elemente
    private ImageButton btnLogout;
    private FloatingActionButton fabAdd;
    // Die Textfelder für die Zahlen (IDs aus deiner activity_home.xml)
    private TextView tvBalance, tvIncome, tvExpense;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Firebase initialisieren
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Views verbinden
        btnLogout = findViewById(R.id.btn_logout);
        fabAdd = findViewById(R.id.fab_add_transaction);

        // --- DIE IDS AUS DEM XML-LAYOUT DEINES PARTNERS ---
        tvBalance = findViewById(R.id.tv_balance);        // Gesamtsaldo
        tvIncome = findViewById(R.id.tv_income_amount);   // Einnahmen
        tvExpense = findViewById(R.id.tv_expense_amount); // Ausgaben
        // -------------------------------------------------

        // Logout-Button Logik (erweitert um Firebase Logout)
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut(); // Nutzer bei Firebase ausloggen
            Intent i = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
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
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Prüfen: Hat dieser User schon irgendwelche Kategorien?
        db.collection("categories")
                .whereEqualTo("userId", user.getUid())
                .limit(1) // Es reicht zu wissen, ob EINE existiert
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Liste leer -> Neuer User (oder alle gelöscht). Wir legen Standards an.
                        createCategory(user.getUid(), "Lebensmittel", 0);
                        createCategory(user.getUid(), "Miete", 0);
                        createCategory(user.getUid(), "Gehalt", 0);

                        // Optional: Kleiner Hinweis
                        // Toast.makeText(this, "Standard-Kategorien eingerichtet.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // Hilfsmethode zum Speichern einer Kategorie
    private void createCategory(String userId, String name, double limit) {
        // Wir nutzen deinen Category-Konstruktor: (userId, name, limit, current)
        Category cat = new Category(userId, name, limit, 0.0);

        db.collection("categories").add(cat);
    }

    // --- Hauptlogik: Daten aus Firestore laden und berechnen ---
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

        // Abfrage an Firestore: Hole alle Dokumente aus der Sammlung "transactions",
        // aber NUR die, wo das Feld "userId" mit der ID des aktuellen Nutzers übereinstimmt.
        db.collection("transactions")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Erfolg! Wir haben die Daten. Jetzt wird gerechnet.
                    double totalIncome = 0;
                    double totalExpense = 0;

                    // Schleife durch alle gefundenen Transaktionen
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        // Das Dokument in unser Transaction-Objekt umwandeln
                        Transaction transaction = document.toObject(Transaction.class);

                        if (transaction != null) {
                            if ("einnahme".equals(transaction.getType())) {
                                totalIncome += transaction.getAmount();
                            } else if ("ausgabe".equals(transaction.getType())) {
                                totalExpense += transaction.getAmount();
                            }
                        }
                    }

                    // Gesamtsaldo berechnen
                    double balance = totalIncome - totalExpense;

                    // UI aktualisieren (die Zahlen in die Textfelder schreiben)
                    updateUI(balance, totalIncome, totalExpense);
                })
                .addOnFailureListener(e -> {
                    // Fehler beim Laden
                    tvBalance.setText("Fehler");
                    Toast.makeText(HomeActivity.this, "Fehler beim Laden der Daten: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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