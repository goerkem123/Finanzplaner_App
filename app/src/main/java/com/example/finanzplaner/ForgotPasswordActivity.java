package com.example.finanzplaner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSend, btnBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // 1. Firebase holen
        mAuth = FirebaseAuth.getInstance();

        // 2. Views verbinden (Fernbedienung holen)
        etEmail = findViewById(R.id.etResetEmail);
        btnSend = findViewById(R.id.btnResetSend);
        btnBack = findViewById(R.id.btnResetBack);

        // 3. Button: Senden
        btnSend.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Bitte E-Mail eingeben!");
                return;
            }

            // Der einzige wichtige Befehl hier:
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Link gesendet! Prüfe deine Mails.", Toast.LENGTH_LONG).show();
                            // Optional: Wir schicken den User direkt zurück zum Login
                            finish();
                        } else {
                            Toast.makeText(this, "Fehler: E-Mail nicht gefunden.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // 4. Button: Zurück
        btnBack.setOnClickListener(v -> {
            finish(); // Schließt diese Seite einfach wieder
        });
    }
}