package com.example.finanzplaner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreManager {
    // Es gibt NUR eine einzige Instanz (Singleton) → egal wie oft du FirestoreManager.getInstance() aufruft
    private static FirestoreManager instance;

    // Firebase Variablen
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    // Privater Konstruktor (Verhindert "new FirestoreManager()")
    private FirestoreManager() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    // Die öffentliche Methode, um den Manager zu holen
    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }
}
