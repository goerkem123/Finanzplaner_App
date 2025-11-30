package com.example.finanzplaner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReportsActivity extends AppCompatActivity {
    private Button btnGenerate;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        btnGenerate = findViewById(R.id.btn_generate_pdf);

        setupBottomNavigation();

        btnGenerate.setOnClickListener(v -> loadDataAndCreatePdf());
    }

    private void loadDataAndCreatePdf() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        btnGenerate.setEnabled(false); // Button sperren
        btnGenerate.setText("Lade Daten...");

        // Wir wollen nur den aktuellen Monat
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        long startOfMonth = cal.getTimeInMillis();

        db.collection("transactions")
                .whereEqualTo("userId", user.getUid())
                .whereGreaterThanOrEqualTo("timestamp", startOfMonth) // Ab dem 1. des Monats
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Transaction> transactions = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        Transaction t = doc.toObject(Transaction.class);
                        if (t != null) transactions.add(t);
                    }

                    if (transactions.isEmpty()) {
                        Toast.makeText(this, "Keine Daten für diesen Monat!", Toast.LENGTH_SHORT).show();
                        btnGenerate.setEnabled(true);
                        btnGenerate.setText("PDF Erstellen & Teilen");
                        return;
                    }

                    // Daten sind da! Jetzt PDF bauen (Nächster Schritt)
                    createPdf(transactions);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnGenerate.setEnabled(true);
                });
    }

    private void createPdf(List<Transaction> transactions) {
        Toast.makeText(this, "Daten geladen: " + transactions.size() + " Einträge", Toast.LENGTH_SHORT).show();
        btnGenerate.setEnabled(true);
        btnGenerate.setText("PDF Erstellen & Teilen");
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        // Hier ist der Reports-Tab aktiv
        bottomNav.setSelectedItemId(R.id.nav_reports);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_transactions) {
                startActivity(new Intent(this, TransactionsActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_diagramme) {
                startActivity(new Intent(this, DiagrammeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_reports) {
                // sind schon hier
                return true;
            }
            return false;
        });

    }
}