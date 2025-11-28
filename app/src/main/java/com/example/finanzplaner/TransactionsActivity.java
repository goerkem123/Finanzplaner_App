package com.example.finanzplaner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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