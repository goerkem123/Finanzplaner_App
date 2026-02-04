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
// *** Verwaltung und Erstellung von PDF-Berichten ***
public class ReportsActivity extends AppCompatActivity {
    private Button btnGenerate, btnPrevMonth, btnNextMonth;
    private android.widget.TextView tvSelectedMonth;
    private FirebaseAuth mAuth;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        mAuth = FirebaseAuth.getInstance();
        selectedDate = Calendar.getInstance();

        initViews(); // Initialisierung der UI-Elemente
        setupBottomNavigation();
        updateMonthDisplay();
    }
    private void initViews() {
        btnGenerate = findViewById(R.id.btn_generate_pdf);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        tvSelectedMonth = findViewById(R.id.tv_selected_month);

        btnGenerate.setOnClickListener(v -> loadDataAndCreatePdf());

        // Klick auf "<" (Einen Monat zurück)
        btnPrevMonth.setOnClickListener(v -> {
            selectedDate.add(Calendar.MONTH, -1);
            updateMonthDisplay();
        });

        // Klick auf ">" (Einen Monat vor)
        btnNextMonth.setOnClickListener(v -> {
            selectedDate.add(Calendar.MONTH, 1);
            updateMonthDisplay();
        });
    }
    // Aktualisiert die Anzeige des gewählten Zeitraums
    private void updateMonthDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.GERMANY);
        tvSelectedMonth.setText(sdf.format(selectedDate.getTime()));
    }
    // Hauptlogik: Daten laden -> Filtern -> PDF anstoßen
    private void loadDataAndCreatePdf() {
        if (mAuth.getCurrentUser() == null) return;

        btnGenerate.setEnabled(false); // Button sperren (Doppelklicks verhindern)
        btnGenerate.setText("Lade Daten...");

        // 1. Startdatum des gewählten Monats setzen (1. Tag 00:00 Uhr)
        Calendar startOfMonth = (Calendar) selectedDate.clone();
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 0);
        startOfMonth.set(Calendar.MILLISECOND, 0);

        // 2. Daten aus Firestore holen (ab Startdatum)
        FirestoreManager.getInstance().getTransactionsFromDate(startOfMonth.getTime(), new FirestoreCallback<List<Transaction>>() {
            @Override
            public void onCallback(List<Transaction> transactions) {
                // Filtern! Da Firestore evtl. auch Daten aus der Zukunft holt (falls vorhanden)
                // oder wir sicherstellen wollen, dass wir nicht im nächsten Monat landen.
                List<Transaction> filteredList = filterTransactionsForMonth(transactions, startOfMonth);

                if (filteredList.isEmpty()) {
                    Toast.makeText(ReportsActivity.this, "Keine Daten für " + tvSelectedMonth.getText(), Toast.LENGTH_SHORT).show();
                    resetButton();
                    return;
                }
                createPdf(filteredList); // Wenn Daten da sind: PDF generieren
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ReportsActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                resetButton();
            }
        });
    }
    // Filtert die Liste lokal, um sicherzustellen, dass nur Daten des gewählten Monats enthalten sind
    private List<Transaction> filterTransactionsForMonth(List<Transaction> all, Calendar monthCal) {
        List<Transaction> result = new ArrayList<>();
        int targetMonth = monthCal.get(Calendar.MONTH);
        int targetYear = monthCal.get(Calendar.YEAR);

        Calendar tCal = Calendar.getInstance();
        for (Transaction t : all) {
            if (t.getTimestamp() != null) {
                tCal.setTime(t.getTimestamp());
                // Prüfen ob Monat UND Jahr übereinstimmen
                if (tCal.get(Calendar.MONTH) == targetMonth && tCal.get(Calendar.YEAR) == targetYear) {
                    result.add(t);
                }
            }
        }
        return result;
    }
    private void resetButton() {
        btnGenerate.setEnabled(true);
        btnGenerate.setText("PDF Erstellen & Teilen");
    }
    // Erstellt das physische PDF-Dokument mittels Canvas (Zeichnen).
    private void createPdf(List<Transaction> transactions) {
        // Dokument und Seite definieren (A4 Format: 595x842 Punkte)
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        // Zeichenwerkzeuge vorbereiten
        android.graphics.Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        // --- KOPFZEILE (Titel) ---
        paint.setTextSize(24);
        paint.setFakeBoldText(true);
        canvas.drawText("Monatsbericht", 50, 60, paint);
        // Datum unter den Titel
        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.GERMANY);
        String reportMonth = sdf.format(selectedDate.getTime());
        canvas.drawText("Zeitraum: " + reportMonth, 50, 85, paint);
        // Trennlinie ziehen
        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(2);
        canvas.drawLine(50, 100, 545, 100, paint);


        // --- TABELLENINHALT ---
        int y = 140; // Start-Höhe für die erste Zeile
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);

        double totalSum = 0;
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd.MM.", Locale.GERMANY);

        for (Transaction t : transactions) {
            // Spalte 1: Datum - (Links)
            if (t.getTimestamp() != null) {
                String date = dateFmt.format(t.getTimestamp());
                canvas.drawText(date, 50, y, paint);
            }

            // Spalte 2: Titel (gekürzt bei Überlänge) - (Mitte)
            String title = t.getTitle();
            if (title.length() > 30) title = title.substring(0, 27) + "...";
            canvas.drawText(title, 110, y, paint);

            // Spalte 3: Betrag (Färbung rot/grün) - (Rechts)
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
            y += 25; // Zeilenabstand/Zeilenumbruch
        }

        // --- FUSSZEILE (Gesamtsumme Strich & Ergebnis) ---
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
        //Wir formatieren das Datum für den Dateinamen
        SimpleDateFormat nameFormat = new SimpleDateFormat("MMMM_yyyy", Locale.GERMANY);
        String monatName = nameFormat.format(selectedDate.getTime());
        // Datei speichern (im Cache-Ordner, damit wir keine Speicher-Rechte brauchen)
        File file = new File(getCacheDir(), "Finanzbericht_" + monatName + ".pdf");

        try {
            document.writeTo(new FileOutputStream(file));
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
        // Temporäre Leserechte an die Empfänger-App vergeben
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Bericht teilen via"));
    }
    // Konfiguration der unteren Navigationsleiste
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