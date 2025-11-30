package com.example.finanzplaner;

import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

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
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);
        // 1. Firebase initialisieren
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

        // 6. Button Klick
        fabAdd.setOnClickListener(v -> showAddDialog());
        // Klick auf ein Listen-Element -> Bearbeiten/Löschen Dialog öffnen
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // passende Kategorie-Objekt aus der Liste öffnen
            Category selectedCategory = categoryObjectList.get(position);
            showEditDialog(selectedCategory);
        });
    }

    // Laden über den Manager
    private void loadCategories() {
        if (mAuth.getCurrentUser() == null) return;

        FirestoreManager.getInstance().getCategories(new FirestoreCallback<List<Category>>() {
                @Override
                public void onCallback(List<Category> result) {
                    displayList.clear();
                    categoryObjectList.clear();

                    for (Category cat : result) {
                        categoryObjectList.add(cat);
                        displayList.add(cat.getName() + " (Limit: " + cat.getLimit() + "€)");
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ManageCategoriesActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    // Das Pop-up Fenster zum Hinzufügen
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Neue Kategorie");

        // Wir bauen ein Layout für das Fenster (zwei Eingabefelder untereinander)
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10); // Ein bisschen Abstand zum Rand

        final EditText inputName = new EditText(this);
        inputName.setHint("Name (z.B. Urlaub)");
        layout.addView(inputName);

        final EditText inputLimit = new EditText(this);
        inputLimit.setHint("Monatslimit (optional)");
        // Hier sorgen wir dafür, dass eine Zahlentastatur aufgeht
        inputLimit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputLimit);

        builder.setView(layout);

        // Button "Speichern"
        builder.setPositiveButton("Speichern", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String limitStr = inputLimit.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name darf nicht leer sein", Toast.LENGTH_SHORT).show();
                return;
            }
            double limit = 0;

            if (!limitStr.isEmpty()) {
                try {
                    limit = Double.parseDouble(limitStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Ungültiges Limit", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // Speichern (0 wird als "Kein Limit" gespeichert)
            saveNewCategory(name, limit);
        });
        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    //Speichern in Firestore
    private void saveNewCategory(String name, double limit) {
        if (mAuth.getCurrentUser() == null) return;

        Category newCat = new Category(mAuth.getCurrentUser().getUid(), name, limit, 0);

        FirestoreManager.getInstance().addCategory(newCat, new FirestoreCallback<Void>() {
            @Override
            public void onCallback(Void result) {
                Toast.makeText(ManageCategoriesActivity.this, "Gespeichert", Toast.LENGTH_SHORT).show();
                loadCategories(); // Liste neu laden!
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ManageCategoriesActivity.this, "Fehler", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showEditDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(category.getName() + " bearbeiten");

        // Layout erstellen (Ein Eingabefeld für das Limit)
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputLimit = new EditText(this);
        inputLimit.setHint("Neues Limit (0 = Kein Limit)");
        // Das aktuelle Limit direkt in das Feld schreiben, damit man es sieht
        inputLimit.setText(String.valueOf(category.getLimit()));
        inputLimit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputLimit);

        builder.setView(layout);

        //Update über Manager
        builder.setPositiveButton("Speichern", (dialog, which) -> {
            String limitStr = inputLimit.getText().toString().trim();
            double newLimit = 0;
            if (!limitStr.isEmpty()) {
                try {
                    newLimit = Double.parseDouble(limitStr);
                } catch (NumberFormatException e) { return; }
            }

            FirestoreManager.getInstance().updateCategoryLimit(category.getId(), newLimit, new FirestoreCallback<Void>() {
                @Override
                public void onCallback(Void result) {
                    Toast.makeText(ManageCategoriesActivity.this, "Aktualisiert", Toast.LENGTH_SHORT).show();
                    loadCategories(); // Neu laden
                }
                @Override
                public void onFailure(Exception e) { /* Fehler */ }
            });
        });

        // Löschen über Manager
        builder.setNeutralButton("Löschen", (dialog, which) -> {
            FirestoreManager.getInstance().deleteCategory(category.getId(), new FirestoreCallback<Void>() {
                @Override
                public void onCallback(Void result) {
                    Toast.makeText(ManageCategoriesActivity.this, "Gelöscht", Toast.LENGTH_SHORT).show();
                    loadCategories(); // Neu laden
                }
                @Override
                public void onFailure(Exception e) { /* Fehler */ }
            });
        });
        // ABBRECHEN - Botton 3
        builder.setNegativeButton("Abbrechen", null);

        builder.show();
    }
}
