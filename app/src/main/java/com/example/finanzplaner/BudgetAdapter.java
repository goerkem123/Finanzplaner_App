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
// Adapter-Klasse für die RecyclerView auf dem Home-Screen.
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private List<Category> categoryList;
    // Konstruktor: Nimmt die Liste der Kategorien entgegen
    public BudgetAdapter(List<Category> categoryList) {
        this.categoryList = categoryList;
    }
    //Erstellt neue Listen-Einträge (Views), wenn nicht genügend recycelbare Views vorhanden sind.
        @NonNull
        @Override
        public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
            return new BudgetViewHolder(view);
        }
    //Die Kern-Logik: Verknüpft die Daten einer spezifischen Kategorie mit den Views.
        @Override
        public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
            Category cat = categoryList.get(position);

            // Grunddaten setzen (Name und Text-Details)
            holder.tvName.setText(cat.getName());

            double spent = cat.getCurrent();
            double limit = cat.getLimit();

            // // Formatierung des Textes (z.B. "50.00 € ausgegeben von 100.00 €")
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

            // --- Logik für den Fortschrittsbalken und das Ampel-System ---
            if (limit > 0) {
                // Prozentberechnung: (Ausgegeben / Limit) * 100
                int percentage = (int) ((spent / limit) * 100);

                // Balken füllen (Maximalwert ist 100)
                holder.progressBar.setMax(100);
                holder.progressBar.setProgress(Math.min(percentage, 100));
                // Farb-Logik basierend auf dem Füllstand
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
                    // Fallback: Wenn kein Limit gesetzt ist, zeigen wir keinen Balken an
                    holder.tvPercent.setText("");
                    holder.progressBar.setProgress(0);
                }

        }
        // Gibt die Anzahl der Elemente in der Liste zurück (wichtig für die RecyclerView)
        @Override
        public int getItemCount() {
            return categoryList.size();
        }

        //Speichert Referenzen zu den UI-Elementen, damit 'findViewById' nicht
        // jedes Mal neu aufgerufen werden muss (Performance-Optimierung beim Scrollen).
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
