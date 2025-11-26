package com.example.finanzplaner;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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


    }
}