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

    public ExpenseListAdapter(List<Expense> expenseList, Context context) {
        this.expenseList = expenseList;
        this.context = context;
    }

    //update la lista de expenses
    public void updateExpenses(List<Expense> newExpenseList) {
        this.expenseList.clear();
        this.expenseList.addAll(newExpenseList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.textExpenseName.setText(expense.getName());
        holder.textExpenseSum.setText(String.format("%.2f RON", expense.getSum()));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView textExpenseName, textExpenseSum;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializam views
            textExpenseName = itemView.findViewById(R.id.text_expense_name);
            textExpenseSum = itemView.findViewById(R.id.text_expense_amount);
        }
    }
}