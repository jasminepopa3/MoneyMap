package com.example.moneymap;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private List<Expense> expensesList;
    private Context context;

    public ExpenseAdapter(List<Expense> expensesList, Context context) {
        this.expensesList = expensesList;
        this.context = context;
    }

    // Update constructor in ExpenseActivity's onCreate
    // adapter = new ExpenseAdapter(expensesList, this);

    @Override
    public ExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ExpenseViewHolder holder, int position) {
        Expense expense = expensesList.get(position);
        Log.d("Adapter", "Binding expense at position " + position + ": " + expense.getCategory().getName());
        holder.expenseName.setText(expense.getName());  // Assuming the expense has a 'getName' method
        holder.categoryName.setText(expense.getCategory().getName());
        holder.amount.setText( expense.getSum()+ " RON");
        holder.description.setText(expense.getCategory().getDescription());
        holder.color.setText(expense.getCategory().getColor());
    }

    @Override
    public int getItemCount() {
        return expensesList.size();
    }

    public class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView expenseName,categoryName, amount, description, color;

        public ExpenseViewHolder(View itemView) {
            super(itemView);
            expenseName=itemView.findViewById(R.id.expense_name);
            categoryName = itemView.findViewById(R.id.category_name);
            amount = itemView.findViewById(R.id.amount);
            description = itemView.findViewById(R.id.description);
            color = itemView.findViewById(R.id.color);
        }
    }
}
