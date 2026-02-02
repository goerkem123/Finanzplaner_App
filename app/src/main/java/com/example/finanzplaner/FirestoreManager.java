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
    private String requireUserId(FirestoreCallback<?> callback) {
        if (mAuth.getCurrentUser() == null) {
            if (callback != null) callback.onFailure(new Exception("Nicht eingeloggt"));
            return null;
        }
        return mAuth.getCurrentUser().getUid();
    }
    // Methode A: Benutzernamen laden
    public void getUserName(FirestoreCallback<String> callback) {
        String userId = requireUserId(callback);
        if (userId == null) return;

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Der Name steht im Feld "username" (so hatten wir es beim Register gespeichert)
                        String name = documentSnapshot.getString("username");
                        if (name != null && !name.isEmpty()) {
                            callback.onCallback(name);
                        } else {
                            callback.onCallback("Nutzer"); // Fallback
                        }
                    } else {
                        callback.onCallback("Nutzer");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }
    // Methode B: Transaktionen laden (Sortiert nach Datum, neueste zuerst)
    public void getTransactions(FirestoreCallback<List<Transaction>> callback) {
        String userId = requireUserId(callback);
        if (userId == null) return;

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
    // Methode C: Kategorien laden (Alphabetisch sortiert)
    public void getCategories(FirestoreCallback<List<Category>> callback) {
        String userId = requireUserId(callback);
        if (userId == null) return;

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
    // Methode D: Transaktion löschen
    // Wir nutzen Void als Typ, weil wir keine Daten zurückbekommen, nur "Erfolg" oder "Fehler"
    public void deleteTransaction(String transactionId, FirestoreCallback<Void> callback) {
        if (requireUserId(callback) == null) return;

        if (transactionId == null) {
            callback.onFailure(new Exception("ID ist null"));
            return;
        }

        db.collection("transactions").document(transactionId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onCallback(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }
    // Methode E: Neue Transaktion speichern
    public void saveTransaction(Transaction transaction, FirestoreCallback<Void> callback) {
        String userId = requireUserId(callback);
        if (userId == null) return;

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
    // Methode F: Kategorie speichern
    public void addCategory(Category category, FirestoreCallback<Void> callback) {
        String userId = requireUserId(callback);
        if (userId == null) return;

        db.collection("categories")
                .add(category)
                .addOnSuccessListener(doc -> callback.onCallback(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }
    // Methode G: Kategorie löschen
    public void deleteCategory(String categoryId, FirestoreCallback<Void> callback) {
        if (requireUserId(callback) == null) return;

        if (categoryId == null) {
            callback.onFailure(new Exception("ID ist null"));
            return;
        }
        db.collection("categories").document(categoryId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onCallback(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }
    // Methode H: Kategorie Limit aktualisieren
    public void updateCategoryLimit(String categoryId, double newLimit, FirestoreCallback<Void> callback) {
        if (requireUserId(callback) == null) return;

        if (categoryId == null) {
            callback.onFailure(new Exception("ID ist null"));
            return;
        }
        db.collection("categories").document(categoryId)
                .update("limit", newLimit)
                .addOnSuccessListener(aVoid -> callback.onCallback(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }
    // Methode I: Transaktionen ab einem bestimmten Datum laden (für PDF)
    public void getTransactionsFromDate(java.util.Date startDate, FirestoreCallback<List<Transaction>> callback) {
        String userId = requireUserId(callback);
        if (userId == null) return;

        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp", startDate)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Transaction> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        Transaction t = doc.toObject(Transaction.class);
                        if (t != null) {
                            t.setId(doc.getId());
                            list.add(t);
                        }
                    }
                    callback.onCallback(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }
    // Methode J: Wiederkehrende Buchungen prüfen
    // Wir brauchen 'Context' für SharedPreferences und geben einen Integer (Anzahl neuer Buchungen) zurück
    public void checkRecurringTransactions(android.content.Context context, FirestoreCallback<Integer> callback) {
        String userId = requireUserId(null); // Callback ist null, da wir hier keine UI-Fehler werfen wollen
        if (userId == null) return;

        android.content.SharedPreferences prefs = context.getSharedPreferences("FinanzPlanerPrefs", android.content.Context.MODE_PRIVATE);
        long lastCheckMs = prefs.getLong("last_recurring_check", 0);

        java.util.Calendar today = java.util.Calendar.getInstance();
        int currentMonth = today.get(java.util.Calendar.MONTH);

        java.util.Calendar lastCheckDate = java.util.Calendar.getInstance();
        lastCheckDate.setTimeInMillis(lastCheckMs);
        int lastCheckMonth = lastCheckDate.get(java.util.Calendar.MONTH);

        // Wenn wir diesen Monat schon geprüft haben -> Abbrechen (0 neue Buchungen)
        if (lastCheckMs != 0 && currentMonth == lastCheckMonth) {
            if (callback != null) callback.onCallback(0);
            return;
        }

        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("recurring", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        prefs.edit().putLong("last_recurring_check", System.currentTimeMillis()).apply();
                        if (callback != null) callback.onCallback(0);
                        return;
                    }

                    int count = 0;
                    for (DocumentSnapshot doc : querySnapshot) {
                        Transaction original = doc.toObject(Transaction.class);
                        if (original == null) continue;

                        Transaction newTrans = new Transaction(
                                original.getUserId(),
                                original.getTitle(),
                                original.getAmount(),
                                original.getType(),
                                original.getCategory(),
                                new java.util.Date(), // Datum von HEUTE
                                false
                        );

                        db.collection("transactions").add(newTrans);
                        count++;
                    }

                    // Zeitstempel aktualisieren
                    prefs.edit().putLong("last_recurring_check", System.currentTimeMillis()).apply();

                    // Der Activity Bescheid sagen, wie viele Buchungen erstellt wurden
                    if (callback != null) callback.onCallback(count);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

}
