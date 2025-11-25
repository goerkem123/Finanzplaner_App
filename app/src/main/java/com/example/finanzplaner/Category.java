package com.example.finanzplaner;
public class Category {
    private String id;          // Firestore-Dokument-ID
    private String userId;      // UID des Nutzers (optional)
    private String name;        // z.B. "Lebensmittel", "Sport"
    private double limit;       // Monatslimit (0 = kein Limit)
    private double current;     // Aktuell ausgegeben (zum Berechnen der Warnfarben)

    // 🔥 WICHTIG: Leerer Konstruktor für Fir

}
