package com.example.moneymap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseListAdapter extends RecyclerView.Adapter<ExpenseListAdapter.ViewHolder> {
    private List<Expense> expenses;
    private Context context;

    public ExpenseListAdapter(List<Expense> expenses, Context context) {
        this.expenses = expenses;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.textExpenseName.setText(expense.getName());
        holder.textExpenseAmount.setText(String.format("%.2f RON", expense.getSum()));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    // update lista expenses
    public void updateExpenseList(List<Expense> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textExpenseName, textExpenseAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textExpenseName = itemView.findViewById(R.id.text_expense_name);
            textExpenseAmount = itemView.findViewById(R.id.text_expense_amount);
        }
    }
}
