package com.example.moneymap;

import java.io.Serializable;

public class Expense implements Serializable {
    private String name;
    private double sum;
    private String month;
    private String year;


    public Expense(String name, double sum, String month, String year) {
        this.name = name;
        this.sum = sum;
        this.month = month;
        this.year = year;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public double getSum() {
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
                ", name='" + name + '\'' +
                ", sum=" + sum +
                ", month='" + month + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
}

