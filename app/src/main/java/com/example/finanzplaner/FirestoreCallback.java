package com.example.finanzplaner;
import java.util.List;
public interface FirestoreCallback<T>{
    void onCallback(T result);
    void onFailure(Exception e);
}
// Ein generisches Interface, damit wir es für Transaktionen UND Kategorien nutzen können
// <T> ist ein Platzhalter für den Datentyp (z.B. List<Transaction>)