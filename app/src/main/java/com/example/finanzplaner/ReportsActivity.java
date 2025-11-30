package com.example.finanzplaner;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        // Dokument erstellen (A4 Größe: 595 x 842 Pixel)
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        // "Stift" vorbereiten
        android.graphics.Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        //Titel schreiben
        paint.setTextSize(24);
        paint.setFakeBoldText(true);
        canvas.drawText("Monatsbericht", 50, 60, paint);
        // Datum unter den Titel
        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.GERMANY);
        String currentMonth = sdf.format(new Date());
        canvas.drawText("Zeitraum: " + currentMonth, 50, 85, paint);
        // Linie ziehen
        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(2);
        canvas.drawLine(50, 100, 545, 100, paint);
        // ... (Fortsetzung)

        // Liste zeichnen
        int y = 140; // Start-Höhe für die erste Zeile
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
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