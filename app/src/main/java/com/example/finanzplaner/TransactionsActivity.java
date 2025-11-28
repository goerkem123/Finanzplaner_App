package com.example.finanzplaner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class TransactionsActivity extends AppCompatActivity {

    // UI Elemente
    private EditText etSearch;
    private Spinner spinnerCategory;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNav;

    // Logik
    private TransactionsAdapter adapter;
    private List<Transaction> transactionList;
    private List<String> categoryList; // Für den Spinner

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        setupBottomNavigation();
        loadData();
    }
    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        spinnerCategory = findViewById(R.id.spinner_filter_category);
        recyclerView = findViewById(R.id.recycler_view_transactions);
        bottomNav = findViewById(R.id.bottomNavigationView);
    }
    private void loadData() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        // Kategorien für den Spinner laden
        db.collection("categories")
                .whereEqualTo("userId", userId)
                .orderBy("name") // Alphabetisch sortieren
                .get()
                .addOnSuccessListener(snapshots -> {
                    categoryList = new ArrayList<>();
                    categoryList.add("Alle"); // Die Option zum Zurücksetzen

                    for (DocumentSnapshot doc : snapshots) {
                        Category c = doc.toObject(Category.class);
                        if (c != null) categoryList.add(c.getName());
                    }

                    // Spinner füllen
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
                    spinnerCategory.setAdapter(spinnerAdapter);
                });
        // Transaktionen für die Liste laden
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING) // Neueste zuerst
                .get()
                .addOnSuccessListener(snapshots -> {
                    transactionList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        Transaction t = doc.toObject(Transaction.class);
                        if (t != null) transactionList.add(t);
                    }

                    // Dem Adapter die Daten geben
                    adapter.updateData(transactionList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Fehler beim Laden: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionList = new ArrayList<>();
        // Leere Liste übergeben, Daten kommen später
        adapter = new TransactionsAdapter(transactionList);
        recyclerView.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        // Hier ist "Transaktionen" der aktuelle Tab
        bottomNav.setSelectedItemId(R.id.nav_transactions);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_transactions) {
                // sind schon hier
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
    }
}