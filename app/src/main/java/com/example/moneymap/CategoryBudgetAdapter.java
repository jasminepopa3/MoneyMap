package com.example.moneymap;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CategoryBudgetAdapter extends RecyclerView.Adapter<CategoryBudgetAdapter.CategoryBudgetViewHolder> {

    private List<Category> categories;
    private OnBudgetSetListener listener;

    public interface OnBudgetSetListener {
        void onBudgetSet(Category category, double budget);
    }

    public CategoryBudgetAdapter(List<Category> categories, OnBudgetSetListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    // Metodă pentru actualizarea listei de categorii
    public void updateCategories(List<Category> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
        notifyDataSetChanged(); // Notifică RecyclerView despre schimbări
    }

    @NonNull
    @Override
    public CategoryBudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new CategoryBudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryBudgetViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.textViewCategoryName.setText(category.getName());

        // Setează culoarea de fundal
        try {
            holder.itemView.setBackgroundColor(Color.parseColor(category.getColor()));
        } catch (IllegalArgumentException e) {
            Log.e("CategoryBudgetAdapter", "Invalid color: " + category.getColor());
            holder.itemView.setBackgroundColor(Color.LTGRAY); // Culoare implicită în caz de eroare
        }

        // Afișează bugetul (dacă există)
        double budget = category.getBudget(); // Presupunem că ai un câmp `budget` în clasa `Category`
        holder.editTextBudget.setText(String.valueOf(budget));

        // Deschide dialogul pentru introducerea bugetului
        holder.budgetContainer.setOnClickListener(v -> showBudgetDialog(holder.itemView.getContext(), category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryBudgetViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCategoryName;
        EditText editTextBudget;
        TextView textViewCurrency;
        LinearLayout budgetContainer; // Referință la layout-ul care conține bugetul și săgeata

        public CategoryBudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategoryName = itemView.findViewById(R.id.textViewCategoryName);
            editTextBudget = itemView.findViewById(R.id.editTextBudget);
            textViewCurrency = itemView.findViewById(R.id.textViewCurrency);
            budgetContainer = itemView.findViewById(R.id.budgetContainer); // ID-ul layout-ului
        }
    }

    private void showBudgetDialog(Context context, Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set Budget for " + category.getName());

        // Creează un EditText pentru introducerea bugetului
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(category.getBudget())); // Precompletează cu bugetul curent
        builder.setView(input);

        // Butonul de salvare
        builder.setPositiveButton("Save", (dialog, which) -> {
            try {
                double budget = Double.parseDouble(input.getText().toString());
                listener.onBudgetSet(category, budget); // Trimite bugetul către activitate
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid budget value", Toast.LENGTH_SHORT).show();
            }
        });

        // Butonul de anulare
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}