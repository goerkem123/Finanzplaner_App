package com.example.finanzplaner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    }
}