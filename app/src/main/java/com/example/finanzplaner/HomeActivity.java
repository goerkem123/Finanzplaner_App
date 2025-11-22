package com.example.finanzplaner;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hier verknüpfst du die Activity mit deinem Home-Layout:
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        // Home ist hier der aktuelle Tab
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Wir sind schon auf Home, nichts machen
                return true;
            } else if (id == R.id.nav_transactions) {
                // Zu Transaktionen wechseln
                Intent intent = new Intent(HomeActivity.this, TransactionsActivity.class);
                startActivity(intent);
                // Optional: HomeActivity schließen, damit "Zurück" nicht zurück auf Home führt
                // finish();
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