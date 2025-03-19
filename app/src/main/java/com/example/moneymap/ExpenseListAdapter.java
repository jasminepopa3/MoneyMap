package com.example.moneymap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseListAdapter extends RecyclerView.Adapter<ExpenseListAdapter.ExpenseViewHolder> {
    private List<Expense> expenseList;
    private Context context;

    // Constructor to initialize the adapter with a list of expenses
    public ExpenseListAdapter(List<Expense> expenseList, Context context) {
        this.expenseList = expenseList;
        this.context = context;
    }

    // Method to update the list of expenses and notify the adapter of the changes
    public void updateExpenses(List<Expense> newExpenseList) {
        this.expenseList.clear(); // Clear the existing list
        this.expenseList.addAll(newExpenseList); // Add the new list of expenses
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        // Bind the data to the views for each expense item
        Expense expense = expenseList.get(position);
        holder.textExpenseName.setText(expense.getName());
        holder.textExpenseSum.setText(String.format("%.2f RON", expense.getSum())); // Format the sum as a currency value
    }

    @Override
    public int getItemCount() {
        // Return the number of items in the list
        return expenseList.size();
    }

    // ViewHolder class to hold the views for each expense item
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView textExpenseName, textExpenseSum;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the views
            textExpenseName = itemView.findViewById(R.id.text_expense_name);
            textExpenseSum = itemView.findViewById(R.id.text_expense_amount);
        }
    }
}