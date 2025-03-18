package com.example.moneymap;

import java.io.Serializable;

public class Category implements Serializable {
    private String id; // Adaugă acest câmp
    private String name;
    private String description;
    private String color;
    private double budget; // Câmp nou pentru buget

    // Constructor pentru cazurile în care ID-ul este necunoscut
    public Category(String name, String description, String color) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.budget = 0; // Buget implicit
    }

    // Constructor pentru cazurile în care ID-ul este cunoscut
    public Category(String id, String name, String description, String color) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.budget = 0; // Buget implicit
    }

    // Getters și setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getColor() {
        return color;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    @Override
    public String toString() {
        return name; // Return the category name to display in the spinner
    }

}