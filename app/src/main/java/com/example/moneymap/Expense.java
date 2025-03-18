package com.example.moneymap;

import java.io.Serializable;

public class Expense implements Serializable {
    private Category category;  // Change category from String to Category model
    private String name;
    private float sum;
    private String month;
    private String year;

    // Constructor for Expense with a Category model
    public Expense(Category category, String name, float sum, String month, String year) {
        this.category = category;
        this.name = name;
        this.sum = sum;
        this.month = month;
        this.year = year;
    }

    // Getters and Setters
    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public float getSum() {
        return sum;
    }

    public String getMonth() {
        return month;
    }

    public String getYear() {
        return year;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "category=" + category.getName() +
                ", name='" + name + '\'' +
                ", sum=" + sum +
                ", month='" + month + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
}

