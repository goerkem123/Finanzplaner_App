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

    public TransactionsAdapter(List<Transaction> transactionList) {
        this.allTransactions = new ArrayList<>(transactionList);
        this.displayedTransactions = transactionList;
    }
    public void updateData(List<Transaction> newList) {
        this.allTransactions = new ArrayList<>(newList);
        this.displayedTransactions = newList;
        notifyDataSetChanged();
    }
    public void filter(String query, String category) {
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

            // Suche im Datum (Timestamp in Text umwandeln: "27.11.2025")
            String dateStr = sdf.format(new Date(t.getTimestamp()));
            boolean matchesDate = dateStr.contains(lowerCaseQuery);

            // Treffer, wenn Titel ODER Datum passt
            matchesSearch = matchesTitle || matchesDate;
        }
        }
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Hier laden wir dein Design (item_transaction.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction t = displayedTransactions.get(position);

        holder.tvTitle.setText(t.getTitle());

        // Datum formatieren (von Millisekunden zu "27.11.2023")
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        String dateStr = sdf.format(new Date(t.getTimestamp()));

        holder.tvDateCat.setText(dateStr + " • " + t.getCategory());

        // Betrag formatieren (z.B. "12,50 €")
        String amountStr = String.format(Locale.GERMANY, "%.2f €", t.getAmount());

        if ("einnahme".equals(t.getType())) {
            holder.tvAmount.setText("+ " + amountStr);
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Grün
        } else {
            holder.tvAmount.setText("- " + amountStr);
            holder.tvAmount.setTextColor(Color.parseColor("#D74848")); // Rot
        }
    }

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
