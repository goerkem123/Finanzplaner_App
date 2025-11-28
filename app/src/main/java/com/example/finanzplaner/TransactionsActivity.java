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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TransactionsActivity extends AppCompatActivity {

    // UI Elemente
    private EditText etSearch;
    private Spinner spinnerCategory;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        bottomNav = findViewById(R.id.bottomNavigationView);

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