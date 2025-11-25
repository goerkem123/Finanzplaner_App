package com.example.finanzplaner;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class AddTransactionActivity extends AppCompatActivity {

    // --- 1. Variablen für die Elemente aus dem Design deines Kumpels ---
    private Button btnIncome, btnExpense;
    private EditText etAmount, etDescription;
    private Spinner spinnerCategory;
    private TextView tvPickDate;
    private Switch switchRecurring;
    private FloatingActionButton fabSave;
    private ArrayAdapter<String> categoryAdapter;
    private java.util.List<String> categoryList;

    // --- 2. Firebase ---
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // --- 3. Status-Variablen (zum Merken der Auswahl) ---
    private String selectedType = "ausgabe"; // Standard: Ausgabe ist aktiv
    private long selectedTimestamp; // Das ausgewählte Datum in Millisekunden
    private Calendar calendar; // Hilfsobjekt für das Datum

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // WICHTIG: Hier wird das Design deines Kumpels geladen!
        setContentView(R.layout.activity_add_transaction);

        // Firebase initialisieren
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Kalender vorbereiten (aktuelles Datum)
        calendar = Calendar.getInstance();
        selectedTimestamp = calendar.getTimeInMillis();

        // --- 4. Die Java-Variablen mit den IDs aus dem XML-Design verbinden ---
        initViews();

        // --- 5. Die Logik für die Knöpfe einrichten ---
        setupTypeButtons();      // Einnahme/Ausgabe Klick-Logik
        setupCategorySpinner();  // Kategorien in den Spinner laden
        setupDatePicker();       // Datumsauswahl-Dialog
        updateDateLabel();       // Das aktuelle Datum im Textfeld anzeigen

        // Der Speichern-Button (FAB)
        fabSave.setOnClickListener(v -> saveTransaction());
    }

    // ==================================================================================
    // HILFSMETHODEN (Machen den Code übersichtlicher)
    // ==================================================================================

    // Verbindet die Variablen oben mit den IDs aus der XML-Datei
    private void initViews() {
        btnIncome = findViewById(R.id.btn_income);
        btnExpense = findViewById(R.id.btn_expense);
        etAmount = findViewById(R.id.et_amount);
        etDescription = findViewById(R.id.et_description);
        spinnerCategory = findViewById(R.id.spinner_category);
        tvPickDate = findViewById(R.id.tv_pick_date);
        switchRecurring = findViewById(R.id.switch_recurring);
        fabSave = findViewById(R.id.fab_save_transaction);
    }

    // Die Logik für die Einnahme/Ausgabe Buttons
    private void setupTypeButtons() {
        // Klick auf "Einnahme"
        btnIncome.setOnClickListener(v -> {
            selectedType = "einnahme";
            // Visuelles Feedback: Einnahme hell, Ausgabe etwas dunkler
            btnIncome.setAlpha(1.0f);
            btnExpense.setAlpha(0.5f);
        });

        // Klick auf "Ausgabe"
        btnExpense.setOnClickListener(v -> {
            selectedType = "ausgabe";
            // Visuelles Feedback: Ausgabe hell, Einnahme etwas dunkler
            btnExpense.setAlpha(1.0f);
            btnIncome.setAlpha(0.5f);
        });

        // Startzustand: Simuliere Klick auf "Ausgabe", damit es am Anfang aktiv ist
        btnExpense.performClick();
    }

    // Füllt den Kategorie-Spinner mit einer Liste
    private void setupCategorySpinner() {
        // 1) Leere Liste anlegen
        categoryList = new ArrayList<>();

        // 2) Adapter erstellen und mit Spinner verbinden
        categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categoryList
        );
        spinnerCategory.setAdapter(categoryAdapter);

    }

    // Die Logik für den Datums-Wähler
    private void setupDatePicker() {
        // Was passiert, wenn ein Datum ausgewählt wird?
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Das neue Datum speichern und anzeigen
            selectedTimestamp = calendar.getTimeInMillis();
            updateDateLabel();
        };

        // Wenn man auf den Text "Datum wählen" klickt, öffne den Dialog
        tvPickDate.setOnClickListener(v -> new DatePickerDialog(AddTransactionActivity.this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    // Aktualisiert den Text im Datums-Feld (z.B. "24.11.2023")
    private void updateDateLabel() {
        String myFormat = "dd.MM.yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
        tvPickDate.setText(sdf.format(calendar.getTime()));
    }


    // ==================================================================================
    // HAUPTLOGIK: SPEICHERN IN FIREBASE
    // ==================================================================================

    private void saveTransaction() {
        // 1. Eingaben aus dem Design auslesen
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        boolean isRecurring = switchRecurring.isChecked();

        // 2. Prüfen: Hat der Nutzer alles ausgefüllt?
        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Bitte Betrag eingeben");
            return;
        }
        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Bitte Beschreibung eingeben");
            return;
        }

        // Den Betrag von Text in eine Zahl umwandeln
        double amount = Double.parseDouble(amountStr);

        // 3. Prüfen: Ist der Nutzer eingeloggt?
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Fehler: Nicht eingeloggt!", Toast.LENGTH_SHORT).show();
            finish(); // Fenster schließen
            return;
        }

        // Feedback geben und Button sperren (gegen Doppelklicks)
        Toast.makeText(this, "Speichere...", Toast.LENGTH_SHORT).show();
        fabSave.setEnabled(false);

        // 4. Das Transaction-Objekt erstellen (unser "Bauplan")
        Transaction newTransaction = new Transaction(
                user.getUid(),
                description,
                amount,
                selectedType,
                category,
                selectedTimestamp,
                isRecurring
        );

        // 5. Ab zu Firestore! In die Sammlung "transactions" speichern.
        db.collection("transactions")
                .add(newTransaction)
                .addOnSuccessListener(documentReference -> {
                    // ERFOLG!
                    Toast.makeText(AddTransactionActivity.this, "Transaktion gespeichert!", Toast.LENGTH_SHORT).show();
                    finish(); // Fenster schließen und zurück zum Dashboard
                })
                .addOnFailureListener(e -> {
                    // FEHLER!
                    Toast.makeText(AddTransactionActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    fabSave.setEnabled(true); // Button wieder aktivieren für neuen Versuch
                });
    }
}