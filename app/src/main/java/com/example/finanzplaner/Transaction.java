package com.example.finanzplaner;

// Diese Klasse ist der Bauplan für eine einzelne Transaktion.
// Firestore kann Objekte dieser Klasse direkt speichern und laden.
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
public class Transaction {

    // Die Eigenschaften einer Transaktion
    private String id;          // Die eindeutige ID des Firestore-Dokuments
    private String userId;      // Die UID des Nutzers, dem das gehört
    private String title;       // Kurzbeschreibung (z.B. "Mittagessen")
    private double amount;      // Der Betrag (z.B. 12.50)
    private String type;        // "einnahme" oder "ausgabe"
    private String category;    // z.B. "Lebensmittel", "Miete"
    @ServerTimestamp // Das sagt Firebase: "Wenn null, nimm Serverzeit"
    private Date timestamp;
    private boolean isRecurring; // Ist es eine wiederkehrende Buchung?

    // 1. WICHTIG: Leerer Konstruktor (wird von Firestore benötigt!)
    public Transaction() {
        // Muss leer bleiben
    }

    // 2. Voller Konstruktor (aktualisiert mit timestamp UND isRecurring)
    public Transaction(String userId, String title, double amount, String type, String category, Date timestamp, boolean isRecurring) {
        // ID wird erst später gesetzt, wenn wir speichern
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.timestamp = timestamp;

        this.isRecurring = isRecurring;
    }

    // 3. Getter und Setter (damit Firestore auf die privaten Variablen zugreifen kann)

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }


    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }
}