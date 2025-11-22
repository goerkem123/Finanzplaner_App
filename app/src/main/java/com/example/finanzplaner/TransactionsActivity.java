package com.example.finanzplaner;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TransactionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        // Hier ist "Transaktionen" der aktuelle Tab
        bottomNav.setSelectedItemId(R.id.nav_transactions);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(TransactionsActivity.this, HomeActivity.class);
                startActivity(intent);
                // Optional: aktuelle Transaktionen-Aktivität schließen
                finish();
                return true;
            } else if (id == R.id.nav_transactions) {
                // sind schon hier, nichts tun
                return true;
            } else if (id == R.id.nav_diagramme) {
                // später: DiagrammeActivity starten
                return true;
            } else if (id == R.id.nav_reports) {
                // später: ReportsActivity starten
                return true;
            }
            return false;
        });
    }
}