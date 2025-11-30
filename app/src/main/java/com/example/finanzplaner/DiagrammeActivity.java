package com.example.finanzplaner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        FirestoreManager.getInstance().getTransactions(new FirestoreCallback<List<Transaction>>() {
            @Override
            public void onCallback(List<Transaction> result) {

                // Kategorien zusammenrechnen (Gruppieren)
                Map<String, Double> categorySums = new HashMap<>();

                for (Transaction t : result) {
                    // Filtern: Passt der Typ (einnahme/ausgabe) zur Auswahl?
                    if (t.getType().equals(type)) {
                        String cat = t.getCategory();
                        double amount = t.getAmount();

                        if (categorySums.containsKey(cat)) {
                            categorySums.put(cat, categorySums.get(cat) + amount);
                        } else {
                            categorySums.put(cat, amount);
                        }
                    }
                }

                updateChart(categorySums);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(DiagrammeActivity.this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChart(Map<String, Double> categorySums) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categorySums.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setCenterText("Keine Daten");
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getCustomColors());
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(data);
        pieChart.invalidate();
        pieChart.animateY(1000);

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