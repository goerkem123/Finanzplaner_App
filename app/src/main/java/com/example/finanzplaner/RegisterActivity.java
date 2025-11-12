package com.example.finanzplaner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etPasswordConfirm;
    private Button btnRegister, btnBackToLogin;
    private TextView tvRegisterResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 👉 Hier sagst du Android, welches Layout angezeigt werden soll:
        setContentView(R.layout.activity_register);

        // (optional) nur, wenn du so ein Title-Element hast:
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.RegisterTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1) Views anbinden
        etUsername        = findViewById(R.id.Benutzername);
        etEmail           = findViewById(R.id.RegisterEmail);
        etPassword        = findViewById(R.id.RegisterPassword);
        etPasswordConfirm = findViewById(R.id.RegisterPasswordConfirm);
        btnRegister       = findViewById(R.id.button);        // "Registrieren"
        btnBackToLogin    = findViewById(R.id.BackToLogin);   // "Zurück zum Login"
        tvRegisterResult  = findViewById(R.id.tvRegisterResult);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pw = etPassword.getText().toString();
            String pwConfirm = etPasswordConfirm.getText().toString();

            // Validierung (einfache Prüfungen)
            if (username.isEmpty()) {
                etUsername.setError("Bitte Benutzername eingeben");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Bitte gültige E-Mail eingeben");
                return;
            }

            if (pw.length() < 8) {
                etPassword.setError("Passwort muss mind. 8 Zeichen haben");
                return;
            }

            if (!pw.equals(pwConfirm)) {
                etPasswordConfirm.setError("Passwörter stimmen nicht überein");
                return;
            }

            // Erfolgsmeldung
            String message = "Registrierung erfolgreich!\nWillkommen, " + username + " 👋";
            tvRegisterResult.setText(message);
            Toast.makeText(this, "Erfolgreich registriert", Toast.LENGTH_SHORT).show();

            // Eingabefelder leeren
            etUsername.setText("");
            etEmail.setText("");
            etPassword.setText("");
            etPasswordConfirm.setText("");
        });

        // 3️⃣ Klick auf „Zurück zum Login“
        btnBackToLogin.setOnClickListener(v -> {
            // Starte die LoginActivity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);

            // Beende die aktuelle Activity, damit man nicht doppelt zurück kann
            finish();
        });

    }
}