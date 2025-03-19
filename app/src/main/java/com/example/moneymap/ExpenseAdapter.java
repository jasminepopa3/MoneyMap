package com.example.moneymap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {
    private List<GroupedExpense> groupedExpenses;
    private Context context;
    private String selectedMonth;
    private String selectedYear;
    private ActivityResultLauncher<Intent> detailsLauncher;

    public ExpenseAdapter(List<GroupedExpense> groupedExpenses, Context context, String selectedMonth, String selectedYear, ActivityResultLauncher<Intent> detailsLauncher) {
        this.groupedExpenses = groupedExpenses;
        this.context = context;
        this.selectedMonth = selectedMonth;
        this.selectedYear = selectedYear;
        this.detailsLauncher = detailsLauncher;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grouped_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupedExpense groupedExpense = groupedExpenses.get(position);
        Category category = groupedExpense.getCategory();

        holder.textCategory.setText(category.getName());

        // Set the category color indicator
        if (category.getColor() != null && !category.getColor().isEmpty()) {
            try {
                holder.categoryColorIndicator.setBackgroundColor(Color.parseColor(category.getColor()));
            } catch (IllegalArgumentException e) {
                // Fallback to default color if parsing fails
                holder.categoryColorIndicator.setBackgroundColor(Color.GRAY);
                Log.e("ExpenseAdapter", "Error parsing color: " + category.getColor(), e);
            }
        } else {
            holder.categoryColorIndicator.setBackgroundColor(Color.GRAY);
        }

        // data pentru buget
        fetchBudgetData(holder, category, groupedExpense.getTotalExpense());

        // click pt detalii cheltuieli
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ExpenseDetailsActivity.class);
            intent.putExtra("expenseList", (Serializable) groupedExpense.getExpenses());
            intent.putExtra("categoryId", category.getId());
            intent.putExtra("categoryName", category.getName());
            intent.putExtra("selectedMonth", selectedMonth);
            intent.putExtra("selectedYear", selectedYear);
            detailsLauncher.launch(intent);
        });
    }

    private void fetchBudgetData(ViewHolder holder, Category category, double totalExpense) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).collection("categories")
                    .document(category.getId()).collection("budgets")
                    .document(selectedYear + "_" + selectedMonth)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            //verificam daca bugetul exista pentru user cu luna si anul selectat
                            Double budgetValue = documentSnapshot.getDouble("budget");
                            if (budgetValue != null) {

                                //calculare procent
                                if (budgetValue != 0) {
                                    holder.textTotalAndBudget.setText(String.format("%.2f/%.2f RON", totalExpense, budgetValue));
                                    double percentage = 100 * totalExpense / budgetValue;
                                    holder.textPercentageDifference.setText(String.format("%.2f%%", percentage));
                                    holder.progressBarPercentage.setProgress((int) percentage);

                                } else {
                                    holder.textTotalAndBudget.setText(String.format("%.2f RON", totalExpense));
                                    holder.textPercentageDifference.setText("Bugetul pentru aceasta categorie nu a fost setat");
                                    holder.progressBarPercentage.setProgress(0);
                                }
                            } else {
                                holder.textTotalAndBudget.setText(String.format("%.2f RON", totalExpense));
                                holder.textPercentageDifference.setText("Bugetul pentru aceasta categorie nu a fost setat");
                                holder.progressBarPercentage.setProgress(0);
                            }
                        } else {
                            holder.textTotalAndBudget.setText(String.format("%.2f RON", totalExpense));
                            holder.textPercentageDifference.setText("Bugetul pentru aceasta categorie nu a fost setat");
                            holder.progressBarPercentage.setProgress(0);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching budget data", e);
                        holder.textPercentageDifference.setText("Eroare la incarcarea bugetului");
                    });
        } else {
            holder.textPercentageDifference.setText("Utilizatorul nu este autentificat");
        }
    }

    @Override
    public int getItemCount() {
        return groupedExpenses.size();
    }

    public void updateSelectedDate(String selectedMonth, String selectedYear) {
        this.selectedMonth = selectedMonth;
        this.selectedYear = selectedYear;
        //update la lista de grouped expenses cand se schimba data/anul
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textCategory, textTotalAndBudget, textPercentageDifference;
        ProgressBar progressBarPercentage;
        View categoryColorIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textCategory = itemView.findViewById(R.id.text_category_name);
            textTotalAndBudget = itemView.findViewById(R.id.text_total_and_budget);
            textPercentageDifference = itemView.findViewById(R.id.text_budget_percentage);
            progressBarPercentage = itemView.findViewById(R.id.progress_bar_percentage);
            categoryColorIndicator = itemView.findViewById(R.id.category_color_indicator);
        }
    }
}