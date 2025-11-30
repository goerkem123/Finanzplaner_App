package com.example.finanzplaner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportsActivity extends AppCompatActivity {
    private Button btnGenerate;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        btnGenerate = findViewById(R.id.btn_generate_pdf);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        // Hier ist der Reports-Tab aktiv
        bottomNav.setSelectedItemId(R.id.nav_reports);

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
                startActivity(new Intent(this, DiagrammeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_reports) {
                // sind schon hier
                return true;
            }
            return false;
        });
    }
}