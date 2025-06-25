package com.foodieexpress.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a shopping cart for a customer
 */
public class Cart {
    private String customerId;
    private List<CartItem> items;
    private double subtotal;
    private int totalItems;

    // Constructor
    public Cart(String customerId) {
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.subtotal = 0.0;
        this.totalItems = 0;
    }

    // Add item to cart
    public void addItem(MenuItem menuItem, int quantity) {
        addItem(menuItem, quantity, "");
    }

    // Add item to cart with special instructions
    public void addItem(MenuItem menuItem, int quantity, String specialInstructions) {
        if (menuItem == null || quantity <= 0) {
            return;
        }

        // Check if item already exists in cart (same menu item and instructions)
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getMenuItem().getId() == menuItem.getId() &&
                               item.getSpecialInstructions().equals(specialInstructions != null ? specialInstructions : ""))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update existing item quantity
            existingItem.get().addQuantity(quantity);
        } else {
            // Add new item to cart
            CartItem newItem = new CartItem(menuItem, quantity, specialInstructions);
            items.add(newItem);
        }

        calculateTotals();
    }

    // Remove item from cart
    public boolean removeItem(int menuItemId) {
        boolean removed = items.removeIf(item -> item.getMenuItem().getId() == menuItemId);
        if (removed) {
            calculateTotals();
        }
        return removed;
    }

    // Update item quantity
    public boolean updateItemQuantity(int menuItemId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeItem(menuItemId);
        }

        Optional<CartItem> item = items.stream()
                .filter(cartItem -> cartItem.getMenuItem().getId() == menuItemId)
                .findFirst();

        if (item.isPresent()) {
            item.get().updateQuantity(newQuantity);
            calculateTotals();
            return true;
        }
        return false;
    }

    // Clear all items from cart
    public void clearCart() {
        items.clear();
        calculateTotals();
    }

    // Calculate totals
    private void calculateTotals() {
        this.subtotal = items.stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
        
        this.totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    // Check if cart is empty
    public boolean isEmpty() {
        return items.isEmpty();
    }

    // Get item count
    public int getItemCount() {
        return items.size();
    }

    // Get cart item by menu item ID
    public Optional<CartItem> getCartItem(int menuItemId) {
        return items.stream()
                .filter(item -> item.getMenuItem().getId() == menuItemId)
                .findFirst();
    }

    // Convert cart to order
    public Order convertToOrder(String orderId) {
        if (isEmpty()) {
            return null;
        }
        
        Order order = new Order(orderId, customerId, new ArrayList<>(items));
        return order;
    }

    // Getters
    public String getCustomerId() {
        return customerId;
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public double getSubtotal() {
        return subtotal;
    }

    public int getTotalItems() {
        return totalItems;
    }

    @Override
    public String toString() {
        return String.format("Cart{customerId='%s', itemCount=%d, subtotal=%.2f, totalItems=%d}",
                customerId, items.size(), subtotal, totalItems);
    }

    // Method to display cart in a formatted way
    public String toDisplayString() {
        if (isEmpty()) {
            return "Cart is empty";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Cart for Customer: %s\n", customerId));
        sb.append("=" .repeat(50)).append("\n");
        
        for (int i = 0; i < items.size(); i++) {
            sb.append(String.format("%d. %s\n", i + 1, items.get(i).toDisplayString()));
        }
        
        sb.append("=" .repeat(50)).append("\n");
        sb.append(String.format("Total Items: %d\n", totalItems));
        sb.append(String.format("Subtotal: $%.2f\n", subtotal));
        
        return sb.toString();
    }

    // Get cart summary
    public String getCartSummary() {
        return String.format("Cart: %d items, Subtotal: $%.2f", totalItems, subtotal);
    }
}
