package com.example.finanzplaner;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity {

    // UI-Elemente
    private ListView listView;
    private FloatingActionButton fabAdd;

    // Adapter und Listen für die Daten
    private ArrayAdapter<String> adapter;
    private List<String> displayList;  // Was der Nutzer sieht (z.B. "Miete (500€)")
    private List<Category> categoryObjectList; // Die echten Daten im Hintergrund

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);
        // 1. Firebase initialisieren
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 2. UI verbinden
        listView = findViewById(R.id.listView_categories);
        fabAdd = findViewById(R.id.fab_add_category);

        // 3. Listen vorbereiten
        displayList = new ArrayList<>();
        categoryObjectList = new ArrayList<>();

        // 4. Adapter erstellen (Standard Android Layout für Listen)
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);

        // 5. Daten laden starten
        loadCategories();

        // 6. Button Klick (Bereiten wir schonmal vor, lassen ihn aber noch leer oder machen einen Toast)
        fabAdd.setOnClickListener(v -> {
            Toast.makeText(this, "Hinzufügen kommt im nächsten Schritt!", Toast.LENGTH_SHORT).show();
        });
    }
    // METHODE: DATEN AUS FIREBASE LADEN
    private void loadCategories() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        // Wir nutzen einen "SnapshotListener".
        // Der ist cool, weil er die Liste AUTOMATISCH aktualisiert, wenn sich in der DB was ändert.
        db.collection("categories")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Listen erst leeren, damit nichts doppelt ist
                    displayList.clear();
                    categoryObjectList.clear();

                    if (value != null) {
                        for (DocumentSnapshot doc : value) {
                            // Daten in unser Objekt umwandeln
                            Category cat = doc.toObject(Category.class);

                            // WICHTIG: Die ID speichern, damit wir später löschen können
                            if (cat != null) {
                                cat.setId(doc.getId());

                                categoryObjectList.add(cat);
                                // Hier bauen wir den String, den der Nutzer sieht
                                displayList.add(cat.getName() + " (Limit: " + cat.getLimit() + "€)");
                            }
                        }
                    }
                    // Dem Adapter sagen: "Hey, Daten haben sich geändert, Liste neu malen!"
                    adapter.notifyDataSetChanged();
                });
        // Das Pop-up Fenster zum Hinzufügen
        private void showAddDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Neue Kategorie");

            // Wir bauen ein Layout für das Fenster (zwei Eingabefelder untereinander)
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10); // Ein bisschen Abstand zum Rand
        }
    }
}