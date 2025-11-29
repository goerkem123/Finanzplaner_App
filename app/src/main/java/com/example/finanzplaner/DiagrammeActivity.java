package com.example.finanzplaner;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DiagrammeActivity extends AppCompatActivity {
    private PieChart pieChart;
    private BottomNavigationView bottomNav;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagramme);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        pieChart = findViewById(R.id.pieChart);
        bottomNav = findViewById(R.id.bottomNavigationView);
        
        setupBottomNavigation();

        // Setup Diagramm-Design (Noch ohne Daten)
        setupPieChartStyle();

        // Dieser Tab ist hier aktiv

    }

    private void setupPieChartStyle() {
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_diagramme);
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
                // sind schon hier
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