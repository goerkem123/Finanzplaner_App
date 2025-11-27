package com.example.finanzplaner;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

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
            Category cat = categoryList.get(position);

            // Name setzen
            holder.tvName.setText(cat.getName());

            double spent = cat.getCurrent();
            double limit = cat.getLimit();

            // Einfacher Text für Details
            String details = String.format(Locale.GERMANY, "%.2f € ausgegeben", spent);

            if (limit > 0) {
                details += String.format(Locale.GERMANY, " von %.2f €", limit);
            } else {
                details += " (Kein Limit)";
            }

            holder.tvDetails.setText(details);

            // Reset für den nächsten Schritt (damit keine alten Daten drin stehen)
            holder.tvPercent.setText("");
            holder.progressBar.setProgress(0);

            // Prozentberechnung & Farben
            if (limit > 0) {
                int percentage = (int) ((spent / limit) * 100);

                // Balken setzen (Maximal 100)
                holder.progressBar.setMax(100);
                holder.progressBar.setProgress(Math.min(percentage, 100));
                // --- Die Farb-Ampel ---
                if (percentage >= 100) {
                    // ROT: Limit voll
                    int redColor = Color.parseColor("#D74848");
                    holder.tvPercent.setText("Limit voll!");
                    holder.tvPercent.setTextColor(redColor);
                    holder.progressBar.setProgressTintList(ColorStateList.valueOf(redColor));
                } else if (percentage >= 75) {
                    // ORANGE: Warnung
                    int orangeColor = Color.parseColor("#FFA500");
                    holder.tvPercent.setText(percentage + "%");
                    holder.tvPercent.setTextColor(orangeColor);
                    holder.progressBar.setProgressTintList(ColorStateList.valueOf(orangeColor));
                } else {
                    // GRÜN: Alles gut
                    int greenColor = Color.parseColor("#4CAF50");
                    holder.tvPercent.setText(percentage + "%");
                    holder.tvPercent.setTextColor(greenColor);
                    holder.progressBar.setProgressTintList(ColorStateList.valueOf(greenColor));
                }
            } else {
                    // Kein Limit -> Alles leer lassen
                    holder.tvPercent.setText("");
                    holder.progressBar.setProgress(0);
                }

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
