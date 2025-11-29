package com.example.finanzplaner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class DiagrammeActivity extends AppCompatActivity {
    private PieChart pieChart;
    private TabLayout tabLayout;
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
        tabLayout = findViewById(R.id.tabLayout_type);
        bottomNav = findViewById(R.id.bottomNavigationView);
        
        setupBottomNavigation();
        setupPieChartStyle();
        setupTabs();
        loadChartData("ausgabe"); // Standard-Start
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Linker Tab: Ausgaben
                    pieChart.setCenterText("Ausgaben");
                    loadChartData("ausgabe");
                } else {
                    // Rechter Tab: Einnahmen
                    pieChart.setCenterText("Einnahmen");
                    loadChartData("einnahme");
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    private void loadChartData(String type) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Nur Transaktionen vom gewählten Typ (Einnahme/Ausgabe) laden
        db.collection("transactions")
                .whereEqualTo("userId", user.getUid())
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener(snapshots -> {});
    }

    // Design des Diagramms einstellen
    private void setupPieChartStyle() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);

        // Donut-Style
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);

        // Text in der Mitte
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Ausgaben");
        pieChart.setCenterTextSize(18f);
        pieChart.setCenterTextColor(Color.WHITE);

        // Legende (Erklärung der Farben)
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);
        l.setTextColor(Color.WHITE);
    }
    // Unsere eigene Farbpalette
    private ArrayList<Integer> getCustomColors() {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#E57373")); // Rot
        colors.add(Color.parseColor("#64B5F6")); // Blau
        colors.add(Color.parseColor("#81C784")); // Grün
        colors.add(Color.parseColor("#FFB74D")); // Orange
        colors.add(Color.parseColor("#BA68C8")); // Lila
        colors.add(Color.parseColor("#4DB6AC")); // Türkis
        colors.add(Color.parseColor("#FFF176")); // Gelb
        return colors;
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