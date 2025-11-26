package com.example.finanzplaner;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
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
        // Wir benutzen hier ein Layout mit weißer Schrift, falls der Hintergrund dunkel ist,
        // oder das Standard-Layout. Erstmal Standard: simple_list_item_1
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);

        // 5. Daten laden starten
        loadCategories();

        // 6. Button Klick (Bereiten wir schonmal vor, lassen ihn aber noch leer oder machen einen Toast)
        fabAdd.setOnClickListener(v -> {
            Toast.makeText(this, "Hinzufügen kommt im nächsten Schritt!", Toast.LENGTH_SHORT).show();
        });
    }

    }
}