package com.example.finanzplaner;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {
    private Button btnGenerate;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        mAuth = FirebaseAuth.getInstance();
        btnGenerate = findViewById(R.id.btn_generate_pdf);

        setupBottomNavigation();

        btnGenerate.setOnClickListener(v -> loadDataAndCreatePdf());
    }

    private void loadDataAndCreatePdf() {
        if (mAuth.getCurrentUser() == null) return;

        btnGenerate.setEnabled(false); // Button sperren
        btnGenerate.setText("Lade Daten...");

        // Wir wollen nur den aktuellen Monat
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);

        //Laden über den Manager
        FirestoreManager.getInstance().getTransactionsFromDate(cal.getTime(), new FirestoreCallback<List<Transaction>>() {
            @Override
            public void onCallback(List<Transaction> transactions) {
                if (transactions.isEmpty()) {
                    Toast.makeText(ReportsActivity.this, "Keine Daten für diesen Monat!", Toast.LENGTH_SHORT).show();
                    resetButton();
                    return;
                }
                createPdf(transactions);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ReportsActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                resetButton();
            }
        });
    }
    private void resetButton() {
        btnGenerate.setEnabled(true);
        btnGenerate.setText("PDF Erstellen & Teilen");
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

        double totalSum = 0;
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd.MM.", Locale.GERMANY);

        for (Transaction t : transactions) {
            // Datum (Links)
            if (t.getTimestamp() != null) {
                String date = dateFmt.format(t.getTimestamp());
                canvas.drawText(date, 50, y, paint);
            }

            // Titel (Mitte)
            // Titel wird gekürzt, falls er zu lang ist
            String title = t.getTitle();
            if (title.length() > 30) title = title.substring(0, 27) + "...";
            canvas.drawText(title, 110, y, paint);

            // Betrag (Rechtsbündig simulieren)
            String amountStr = String.format(Locale.GERMANY, "%.2f €", t.getAmount());
            if ("ausgabe".equals(t.getType())) {
                paint.setColor(Color.RED);
                totalSum -= t.getAmount();
                amountStr = "- " + amountStr;
            } else {
                paint.setColor(Color.parseColor("#4CAF50")); // Grün
                totalSum += t.getAmount();
                amountStr = "+ " + amountStr;
            }
            canvas.drawText(amountStr, 450, y, paint);

            // Farbe zurücksetzen für nächste Zeile
            paint.setColor(Color.BLACK);
            y += 25; // Zeilenabstand
        }

        // Gesamtsumme Strich & Ergebnis
        y += 10;
        paint.setColor(Color.BLACK);
        canvas.drawLine(400, y, 545, y, paint); // Kleiner Strich über Summe
        y += 25;

        paint.setFakeBoldText(true);
        canvas.drawText("Gesamt:", 350, y, paint);

        String totalStr = String.format(Locale.GERMANY, "%.2f €", totalSum);
        canvas.drawText(totalStr, 450, y, paint);

        // Seite abschließen
        document.finishPage(page);
        // Datei speichern (im Cache-Ordner, damit wir keine Speicher-Rechte brauchen)
        File file = new File(getCacheDir(), "Finanzbericht.pdf");

        try {
            document.writeTo(new FileOutputStream(file));

            // Teilen-Intent starten
            sharePdf(file); // Hilfsmethode starten

        } catch (IOException e) {
            Toast.makeText(this, "Fehler beim Speichern: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            document.close();
            btnGenerate.setEnabled(true);
            btnGenerate.setText("PDF Erstellen & Teilen");
        }
    }
    // Hilfsmethode zum Teilen
    private void sharePdf(File file) {
        //Nutzen des FileProvider --> im Manifest eingerichtet
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Bericht teilen via"));
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