package com.example.moneymap;

public class Category {
    private String id; // Adaugă acest câmp
    private String name;
    private String description;
    private String color;

    // Constructor pentru cazurile în care ID-ul este necunoscut
    public Category(String name, String description, String color) {
        this.name = name;
        this.description = description;
        this.color = color;
    }

    // Constructor pentru cazurile în care ID-ul este cunoscut
    public Category(String id, String name, String description, String color) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
    }

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
}