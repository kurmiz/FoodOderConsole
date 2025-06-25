package com.foodieexpress.model;

import java.util.Objects;

/**
 * Represents an item in the shopping cart
 */
public class CartItem {
    private MenuItem menuItem;
    private int quantity;
    private String specialInstructions;
    private double totalPrice;

    // Default constructor
    public CartItem() {}

    // Constructor
    public CartItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.specialInstructions = "";
        calculateTotalPrice();
    }

    // Constructor with special instructions
    public CartItem(MenuItem menuItem, int quantity, String specialInstructions) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.specialInstructions = specialInstructions != null ? specialInstructions : "";
        calculateTotalPrice();
    }

    // Calculate total price for this cart item
    private void calculateTotalPrice() {
        this.totalPrice = menuItem.getPrice() * quantity;
    }

    // Getters and Setters
    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
        calculateTotalPrice();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions != null ? specialInstructions : "";
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    // Update quantity and recalculate price
    public void updateQuantity(int newQuantity) {
        if (newQuantity > 0) {
            this.quantity = newQuantity;
            calculateTotalPrice();
        }
    }

    // Add to existing quantity
    public void addQuantity(int additionalQuantity) {
        if (additionalQuantity > 0) {
            this.quantity += additionalQuantity;
            calculateTotalPrice();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(menuItem, cartItem.menuItem) &&
               Objects.equals(specialInstructions, cartItem.specialInstructions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuItem, specialInstructions);
    }

    @Override
    public String toString() {
        return String.format("CartItem{menuItem=%s, quantity=%d, totalPrice=%.2f, instructions='%s'}",
                menuItem.getName(), quantity, totalPrice, specialInstructions);
    }

    // Method to display cart item in a formatted way
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%dx %s - $%.2f each = $%.2f total",
                quantity, menuItem.getName(), menuItem.getPrice(), totalPrice));
        
        if (!specialInstructions.isEmpty()) {
            sb.append(String.format("\n   Special Instructions: %s", specialInstructions));
        }
        
        return sb.toString();
    }
}
