package com.example.finanzplaner;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

        // Hier kommen später die Daten rein

        @NonNull
        @Override
        public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null; // Kommt im nächsten Schritt
        }

        @Override
        public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
            // Hier kommt die Logik rein
        }

        @Override
        public int getItemCount() {
            return 0;
        }

        // Die innere Klasse, die unsere Views aus dem XML hält
        public static class BudgetViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPercent, tvDetails;
            ProgressBar progressBar;

            public BudgetViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_category_name);
                tvPercent = itemView.findViewById(R.id.tv_category_percent);
                tvDetails = itemView.findViewById(R.id.tv_category_details);
                progressBar = itemView.findViewById(R.id.progress_bar_budget);
            }
        }
}
