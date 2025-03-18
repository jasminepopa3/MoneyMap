package com.example.moneymap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GroupedExpense implements Serializable {
    private Category category;
    private List<Expense> expenses = new ArrayList<>();
    private double totalAmount;

    public GroupedExpense(Category category) {
        this.category = category;
        this.totalAmount = 0;
    }

    public void addExpense(Expense expense) {
        this.expenses.add(expense);
        this.totalAmount += expense.getSum();
    }

    public Category getCategory() {
        return category;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public double getTotalExpense() {
        return totalAmount;
    }
}