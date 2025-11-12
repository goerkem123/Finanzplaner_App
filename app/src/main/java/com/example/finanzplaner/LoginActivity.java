package com.example.finanzplaner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    // View-Referenzen
    private EditText etemail, etpassword;
    private Button btnlogin, btnGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.LoginTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1) Views verbinden
        etemail = findViewById(R.id.email);
        etpassword = findViewById(R.id.password);
        btnlogin = findViewById(R.id.login);
        btnGoToRegister = findViewById(R.id.GoToRegister);

// 2) Login-Klick
        btnlogin.setOnClickListener(v -> {
            String email = etemail.getText().toString().trim();
            String pw = etpassword.getText().toString();

            if (email.isEmpty()) {
                etemail.setError("Bitte E-Mail eingeben");
                return;
            }
            if (pw.isEmpty()) {
                etpassword.setError("Bitte Passwort eingeben");
                return;
            }

            // TODO: später echte Prüfung (DB/Firebase)
            Toast.makeText(this, "Login gedrückt: " + email, Toast.LENGTH_SHORT).show();
        });

        // 3) Zur Registrierung navigieren
        btnGoToRegister.setOnClickListener(v -> {
            Intent i = new Intent(this, RegisterActivity.class);
            startActivity(i);
        });
    }
}