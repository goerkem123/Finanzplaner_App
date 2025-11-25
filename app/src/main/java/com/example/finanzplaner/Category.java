package com.example.finanzplaner;
public class Category {
    private String id;          // Firestore-Dokument-ID
    private String userId;      // UID des Nutzers (optional)
    private String name;        // z.B. "Lebensmittel", "Sport"
    private double limit;       // Monatslimit (0 = kein Limit)
    private double current;     // Aktuell ausgegeben (zum Berechnen der Warnfarben)

    public Category() {}

    // Voller Konstruktor
    public Category(String userId, String name, double limit, double current) {
        this.userId = userId;
        this.name = name;
        this.limit = limit;
        this.current = current;
    }
    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLimit() { return limit; }
    public void setLimit(double limit) { this.limit = limit; }

    public double getCurrent() { return current; }
    public void setCurrent(double current) { this.current = current; }
}
