package com.example.finanzplaner;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>{
    // Zwei Listen: Eine für ALLE Daten (Backup) und eine für die ANZEIGE
    private List<Transaction> allTransactions;
    private List<Transaction> displayedTransactions;
    private OnTransactionLongClickListener onLongClickListener;
    public interface OnTransactionLongClickListener { //Definiert, dass beim Klick das Daten-Paket (Transaction) mitgeschickt wird.
        void onTransactionLongClick(Transaction transaction);
    }

    // Hier klinkt sich die Activity ein, um die Klick-Nachrichten zu empfangen.
    public void setOnTransactionLongClickListener(OnTransactionLongClickListener listener) {
        this.onLongClickListener = listener;
    }

    public TransactionsAdapter(List<Transaction> transactionList) { // Startschuss für den Adapter. Der Konstruktor läuft genau einmal, wenn der Adapter erstellt wird
        this.allTransactions = new ArrayList<>(transactionList); // Wir klonen die Liste, damit wir beim Filtern immer alle Originaldaten behalten
        this.displayedTransactions = transactionList; // Das ist die Liste, die tatsächlich angezeigt wird
    }
    public void updateData(List<Transaction> newList) { // Ersetzt alle alten Daten mit der neuen Liste aus der Datenbank
        this.allTransactions = new ArrayList<>(newList);
        this.displayedTransactions = newList;
        notifyDataSetChanged();
    }
    public void filter(String query, String category) { // Durchsucht das Backup (allTransactions) nach Suchtext UND Kategorie.
        List<Transaction> filteredList = new ArrayList<>();

        String lowerCaseQuery = query.toLowerCase().trim();

        boolean isCategoryAll = category.equals("Alle") || category.isEmpty();

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        for (Transaction t : allTransactions) {
        // Die Suche (String ODER Datum)
        boolean matchesSearch;
        if (lowerCaseQuery.isEmpty()) {
            // Wenn nichts im Suchfeld steht, passt "alles"
            matchesSearch = true;
        } else {
            // Suche im Titel
            boolean matchesTitle = t.getTitle().toLowerCase().contains(lowerCaseQuery);

            // Suche im Datum
            boolean matchesDate = false;
            if (t.getTimestamp() != null) {
                // KORREKTUR: Direkt das Date-Objekt nutzen, kein "new Date()" mehr!
                String dateStr = sdf.format(t.getTimestamp());
                matchesDate = dateStr.contains(lowerCaseQuery);
            }

            // Treffer, wenn Titel ODER Datum passt
            matchesSearch = matchesTitle || matchesDate;
        }
            // Der Filter (Kategorie)
            boolean matchesCategory = isCategoryAll || t.getCategory().equals(category);
            // Kombination (UND)
            if (matchesSearch && matchesCategory) {
                filteredList.add(t);
            }
        }
        // Liste austauschen und UI aktualisieren
        this.displayedTransactions = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Hier laden wir dein Design (item_transaction.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) { //Hier befüllen wir die leeren Zeilen mit den tatsächlichen Daten
        Transaction t = displayedTransactions.get(position);

        holder.tvTitle.setText(t.getTitle());

        // In diesem Block kümmern wir uns um die Benutzerfreundlichkeit
        // Datum formatieren (von Millisekunden zu "27.11.2023")
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        if (t.getTimestamp() != null) {
            String dateStr = sdf.format(t.getTimestamp()); // Direkt reinwerfen!
            holder.tvDateCat.setText(dateStr + " • " + t.getCategory());
        } else {
            holder.tvDateCat.setText("Datum unbekannt • " + t.getCategory());
        }

        // Betrag formatieren (z.B. "12,50 €")
        String amountStr = String.format(Locale.GERMANY, "%.2f €", t.getAmount());

        if ("einnahme".equals(t.getType())) {
            holder.tvAmount.setText("+ " + amountStr);
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Grün
        } else {
            holder.tvAmount.setText("- " + amountStr);
            holder.tvAmount.setTextColor(Color.parseColor("#D74848")); // Rot
        }
        // langen Klick auf das Element aktivieren
        holder.itemView.setOnLongClickListener(v -> {
            if (onLongClickListener != null) {
                onLongClickListener.onTransactionLongClick(t);
            }
            return true;
        });
    }

    // Mit getItemCount teilen wir dem RecyclerView mit, wie viele Elemente er darstellen soll.
    @Override
    public int getItemCount() {
        return displayedTransactions.size();
    }

    // Die innere Klasse, die die Views hält
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDateCat, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_trans_title);
            tvDateCat = itemView.findViewById(R.id.tv_trans_date_cat);
            tvAmount = itemView.findViewById(R.id.tv_trans_amount);
        }
    }
}
