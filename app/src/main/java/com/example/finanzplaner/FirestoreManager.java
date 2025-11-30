package com.example.finanzplaner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

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
    // Methode A: Transaktionen laden (Sortiert nach Datum, neueste zuerst)
    public void getTransactions(FirestoreCallback<List<Transaction>> callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onFailure(new Exception("User ist nicht eingeloggt"));
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Transaction> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        Transaction t = doc.toObject(Transaction.class);
                        if (t != null) {
                            t.setId(doc.getId()); // WICHTIG: Dokument-ID setzen für Lösch-Funktion
                            list.add(t);
                        }
                    }
                    // Erfolg: Liste zurückgeben
                    callback.onCallback(list);
                })
                .addOnFailureListener(e -> {
                    // Fehler weiterleiten
                    callback.onFailure(e);
                });
    }
    // Methode B: Kategorien laden (Alphabetisch sortiert)
    public void getCategories(FirestoreCallback<List<Category>> callback) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("categories")
                .whereEqualTo("userId", userId)
                .orderBy("name")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Category> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        Category c = doc.toObject(Category.class);
                        if (c != null) {
                            c.setId(doc.getId());
                            list.add(c);
                        }
                    }
                    callback.onCallback(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }
    // Methode C: Transaktion löschen
    // Wir nutzen Void als Typ, weil wir keine Daten zurückbekommen, nur "Erfolg" oder "Fehler"
    public void deleteTransaction(String transactionId, FirestoreCallback<Void> callback) {
        if (transactionId == null) {
            callback.onFailure(new Exception("ID ist null"));
            return;
        }

        db.collection("transactions").document(transactionId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onCallback(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }
    // Methode D: Neue Transaktion speichern
    public void saveTransaction(Transaction transaction, FirestoreCallback<Void> callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onFailure(new Exception("Nicht eingeloggt"));
            return;
        }

        // Wir fügen das Objekt der Sammlung hinzu
        db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(documentReference -> {
                    // Erfolg! Wir geben null zurück, da wir keine Daten brauchen
                    callback.onCallback(null);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }
    // Methode E: Kategorie speichern
    public void addCategory(Category category, FirestoreCallback<Void> callback) {
        if (mAuth.getCurrentUser() == null) return;

        db.collection("categories").add(category)
                .addOnSuccessListener(doc -> callback.onCallback(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }
}
