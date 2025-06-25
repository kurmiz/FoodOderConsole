package com.foodieexpress.model;

import java.util.Objects;

/**
 * Represents a menu item in the food ordering system
 */
public class MenuItem {
    private int id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String imageUrl;
    private boolean available;

    // Default constructor
    public MenuItem() {}

    // Constructor with all fields
    public MenuItem(int id, String name, String description, double price, String category, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.available = true;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return id == menuItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("MenuItem{id=%d, name='%s', description='%s', price=%.2f, category='%s', available=%s}",
                id, name, description, price, category, available);
    }

    // Method to display menu item in a formatted way
    public String toDisplayString() {
        return String.format("ID: %d | %s - $%.2f\n   Category: %s\n   Description: %s\n   Available: %s",
                id, name, price, category, description, available ? "Yes" : "No");
    }
}
