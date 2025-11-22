package com.example.finanzplaner;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DiagrammeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_diagramme);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        // Dieser Tab ist hier aktiv
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
                // später hinzufügen
                return true;
            }
            return false;
        });
    }
}