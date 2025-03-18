package com.example.moneymap;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.Serializable;


import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {
    private List<GroupedExpense> groupedExpenses;
    private Context context;
    private String selectedMonth;
    private String selectedYear;

    public ExpenseAdapter(List<GroupedExpense> groupedExpenses, Context context, String selectedMonth, String selectedYear) {
        this.groupedExpenses = groupedExpenses;
        this.context = context;
        this.selectedMonth = selectedMonth;
        this.selectedYear = selectedYear;
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
        holder.textTotalExpense.setText(String.format("Total: %.2f RON", groupedExpense.getTotalExpense()));

        // data pentru buget
        fetchBudgetData(holder, category, groupedExpense.getTotalExpense());

        // click pt detalii cheltuieli
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ExpenseDetailsActivity.class);
            intent.putExtra("expenseList", (Serializable) groupedExpense.getExpenses());
            intent.putExtra(("categoryName"),category.getName());
            context.startActivity(intent);
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
                                holder.textCategoryBudget.setText(String.format("Buget: %.2f RON", budgetValue));

                                //calculare procent
                                if (budgetValue != 0) {
                                    double percentage = 100 * totalExpense / budgetValue;
                                    holder.textPercentageDifference.setText(String.format("Consumat: %.2f%%", percentage));
                                } else {
                                    holder.textPercentageDifference.setText("Bugetul pentru aceasta categorie nu a fost setat");
                                }
                            } else {
                                holder.textCategoryBudget.setText("Buget: 0.00 RON");
                                holder.textPercentageDifference.setText("Bugetul pentru aceasta categorie nu a fost setat");
                            }
                        } else {
                            holder.textCategoryBudget.setText("Buget: 0.00 RON");
                            holder.textPercentageDifference.setText("Bugetul pentru aceasta categorie nu a fost setat");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching budget data", e);
                        holder.textCategoryBudget.setText("Buget: 0.00 RON");
                        holder.textPercentageDifference.setText("Eroare la incarcarea bugetului");
                    });
        } else {
            holder.textCategoryBudget.setText("Buget: 0.00 RON");
            holder.textPercentageDifference.setText("Utilizatorul nu este autentificat");
        }
    }

    @Override
    public int getItemCount() {
        return groupedExpenses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textCategory, textTotalExpense, textCategoryBudget, textPercentageDifference;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textCategory = itemView.findViewById(R.id.text_category_name);
            textTotalExpense = itemView.findViewById(R.id.text_total_expense);
            textCategoryBudget = itemView.findViewById(R.id.text_category_budget);
            textPercentageDifference = itemView.findViewById(R.id.text_budget_percentage);
        }
    }
}