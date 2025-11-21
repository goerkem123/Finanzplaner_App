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

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // View-Referenzen
    private EditText etemail, etpassword;
    private Button btnlogin, btnGoToRegister;
    private TextView tvLoginResult;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

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
        tvLoginResult = findViewById(R.id.tvLoginResult);

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

            tvLoginResult.setText("Bitte warten...");
            mAuth.signInWithEmailAndPassword(email, pw)
                    .addOnCompleteListener(this, (Task<AuthResult> task) -> {
                        if (task.isSuccessful()) {
                            tvLoginResult.setText("Login erfolgreich");
                            Toast.makeText(LoginActivity.this, "Willkommen!", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            tvLoginResult.setText("Anmeldung fehlgeschlagen");
                            Toast.makeText(LoginActivity.this, "E-Mail oder Passwort falsch", Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 3) Zur Registrierung navigieren
        btnGoToRegister.setOnClickListener(v -> {
            Intent i = new Intent(this, RegisterActivity.class);
            startActivity(i);
        });
    }
}