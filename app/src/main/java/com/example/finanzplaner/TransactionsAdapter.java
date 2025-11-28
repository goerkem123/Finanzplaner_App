package com.example.finanzplaner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>{
    // Zwei Listen: Eine für ALLE Daten (Backup) und eine für die ANZEIGE
    private List<Transaction> allTransactions;
    private List<Transaction> displayedTransactions;

    public TransactionsAdapter(List<Transaction> transactionList) {
        this.allTransactions = new ArrayList<>(transactionList);
        this.displayedTransactions = transactionList;
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
        // Logik kommt später
    }

    @Override
    public int getItemCount() {
        return 0;
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
