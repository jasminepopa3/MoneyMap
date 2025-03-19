package com.example.moneymap;

import java.io.Serializable;

public class Expense implements Serializable {
    private String name;
    private double sum;
    private String month;
    private String year;
    private String day;



    public Expense(String name, double sum, String day, String month, String year) {
        this.name = name;
        this.sum = sum;
        this.month = month;
        this.year = year;
        this.day=day;
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

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
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

