package com.example.finanzplaner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private ImageButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hier verknüpfst du die Activity mit deinem Home-Layout:
        setContentView(R.layout.activity_home);

        btnLogout = findViewById(R.id.btn_logout);

        btnLogout.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, LoginActivity.class);

            startActivity(i);
            finish(); // HomeActivity schließen
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        // Home ist hier der aktuelle Tab
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // sind schon hier
                return true;
            } else if (id == R.id.nav_transactions) {
                Intent intent = new Intent(this, TransactionsActivity.class);
                startActivity(intent);
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