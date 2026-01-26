package com.example.finanzplaner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // View-Referenzen
    private EditText etemail, etpassword;
    private Button btnlogin, btnGoToRegister;
    private TextView tvLoginResult, tvForgotPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // 1) Views verbinden
        etemail = findViewById(R.id.email);
        etpassword = findViewById(R.id.password);
        btnlogin = findViewById(R.id.login);
        btnGoToRegister = findViewById(R.id.GoToRegister);
        tvLoginResult = findViewById(R.id.tvLoginResult);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

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

                            com.google.firebase.auth.FirebaseUser user = mAuth.getCurrentUser();

                            // Prüfen, ob die E-Mail bereits bestätigt wurde
                            if (user != null && !user.isEmailVerified()) {
                                tvLoginResult.setText("Bitte bestätige zuerst deine E-Mail.");
                                Toast.makeText(this,
                                        "E-Mail wurde noch nicht bestätigt!",
                                        Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                return; // ganz wichtig: Abbrechen!
                            }

                            // Wenn E-Mail bestätigt ist:
                            tvLoginResult.setText("Login erfolgreich");
                            Toast.makeText(LoginActivity.this, "Willkommen!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        }

                        else {
                            tvLoginResult.setText("Anmeldung fehlgeschlagen");
                            Toast.makeText(LoginActivity.this, "E-Mail oder Passwort falsch", Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // LOGIK FÜR PASSWORT VERGESSEN
        tvForgotPassword.setOnClickListener(v -> {
            // Ein Eingabefeld für die E-Mail erstellen
            EditText resetMail = new EditText(v.getContext());

            // Ein Dialog-Fenster bauen
            AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
            passwordResetDialog.setTitle("Passwort zurücksetzen?");
            passwordResetDialog.setMessage("Gib deine E-Mail ein, um den Link zu erhalten.");
            passwordResetDialog.setView(resetMail);

            // "Senden" Button im Dialog
            passwordResetDialog.setPositiveButton("Senden", (dialog, which) -> {
                // E-Mail aus dem Feld holen
                String mail = resetMail.getText().toString().trim();

                if (mail.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Bitte E-Mail eingeben", Toast.LENGTH_SHORT).show();
                    return;
                }

                // FIREBASE BEFEHL: Reset-Mail senden
                mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(unused -> {
                    Toast.makeText(LoginActivity.this, "Reset-Link wurde gesendet! Prüfe deine Mails.", Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            });

            // "Abbrechen" Button im Dialog
            passwordResetDialog.setNegativeButton("Abbrechen", (dialog, which) -> {
                // Fenster einfach schließen
            });

            // Dialog anzeigen
            passwordResetDialog.create().show();
        });

        // 3) Zur Registrierung navigieren
        btnGoToRegister.setOnClickListener(v -> {
            Intent i = new Intent(this, RegisterActivity.class);
            startActivity(i);
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Prüfen, ob der Nutzer schon eingeloggt ist (nicht null)
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Wenn User da ist UND E-Mail bestätigt ist -> Sofort zum Home!
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(i);
            finish(); // Login-Fenster schließen, damit man nicht zurück kann
        }
    }
}