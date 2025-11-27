package com.example.finanzplaner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private List<Category> categoryList;
    // Daten empfangen
    public BudgetAdapter(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

        @NonNull
        @Override
        public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
            return new BudgetViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
            // Hier kommt die Logik rein
        }

        @Override
        public int getItemCount() {
            return categoryList.size();
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
