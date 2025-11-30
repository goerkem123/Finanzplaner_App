package com.example.finanzplaner;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupRecyclerView();
        setupBottomNavigation();
        setupFilterListeners();
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

        // Kategorien laden für Spinner
        FirestoreManager.getInstance().getCategories(new FirestoreCallback<List<Category>>() {
            @Override
            public void onCallback(List<Category> result) {
                categoryList = new ArrayList<>();
                categoryList.add("Alle");
                for (Category c : result) {
                    categoryList.add(c.getName());
                }
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(TransactionsActivity.this, android.R.layout.simple_spinner_dropdown_item, categoryList);
                spinnerCategory.setAdapter(spinnerAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(TransactionsActivity.this, "Fehler Kategorien: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Transaktionen laden
        FirestoreManager.getInstance().getTransactions(new FirestoreCallback<List<Transaction>>() {
            @Override
            public void onCallback(List<Transaction> result) {
                transactionList = result;
                adapter.updateData(transactionList);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(TransactionsActivity.this, "Fehler Transaktionen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionList = new ArrayList<>();
        // Leere Liste übergeben, Daten kommen später
        adapter = new TransactionsAdapter(transactionList);
        adapter.setOnTransactionLongClickListener(transaction -> {
            showDeleteDialog(transaction);
        });
        recyclerView.setAdapter(adapter);
    }

    private void showDeleteDialog(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Löschen?")
                .setMessage("Möchtest du '" + transaction.getTitle() + "' wirklich löschen?")
                .setPositiveButton("Ja, weg damit", (dialog, which) -> {
                    deleteTransaction(transaction);
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void deleteTransaction(Transaction transaction) {
        FirestoreManager.getInstance().deleteTransaction(transaction.getId(), new FirestoreCallback<Void>() {
            @Override
            public void onCallback(Void result) {
                Toast.makeText(TransactionsActivity.this, "Gelöscht!", Toast.LENGTH_SHORT).show();
                // Liste aktualisieren (lädt alles neu)
                loadData();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(TransactionsActivity.this, "Löschen fehlgeschlagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilterListeners() {
        // Suchfeld (reagiert bei jedem Buchstaben)
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(); // Filter sofort anwenden
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        // Spinner (reagiert bei Auswahl-Änderung)
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters(); // Filter sofort anwenden
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    // Werte auslesen und Adapter schicken
    private void applyFilters() {
        // Sicherheitscheck, falls Daten noch laden
        if (adapter == null || spinnerCategory.getSelectedItem() == null) return;

        String query = etSearch.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        // Nur das zeigen was zum Filter und Category
        adapter.filter(query, category);
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