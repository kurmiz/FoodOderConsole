package com.foodieexpress.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an order in the food ordering system
 */
public class Order {
    private String orderId;
    private String customerId;
    private List<CartItem> items;
    private double subtotal;
    private double deliveryFee;
    private double tax;
    private double totalAmount;
    private OrderStatus status;
    private String deliveryType;
    private LocalDateTime orderTime;
    private LocalDateTime estimatedDeliveryTime;
    private String deliveryAddress;
    private String customerPhone;

    // Order status enum
    public enum OrderStatus {
        PENDING, CONFIRMED, PREPARING, ON_THE_WAY, DELIVERED, CANCELLED
    }

    // Default constructor
    public Order() {
        this.items = new ArrayList<>();
        this.orderTime = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }

    // Constructor
    public Order(String orderId, String customerId) {
        this();
        this.orderId = orderId;
        this.customerId = customerId;
    }

    // Constructor with cart items
    public Order(String orderId, String customerId, List<CartItem> cartItems) {
        this(orderId, customerId);
        this.items = new ArrayList<>(cartItems);
        calculateTotals();
    }

    // Calculate all totals
    public void calculateTotals() {
        this.subtotal = items.stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
        
        // Standard delivery fee
        this.deliveryFee = "express".equalsIgnoreCase(deliveryType) ? 6.99 : 3.99;
        
        // 8% tax
        this.tax = subtotal * 0.08;
        
        // Total amount
        this.totalAmount = subtotal + deliveryFee + tax;
        
        // Set estimated delivery time
        int deliveryMinutes = "express".equalsIgnoreCase(deliveryType) ? 20 : 35;
        this.estimatedDeliveryTime = orderTime.plusMinutes(deliveryMinutes);
    }

    // Add item to order
    public void addItem(CartItem item) {
        // Check if item already exists (same menu item and instructions)
        CartItem existingItem = items.stream()
                .filter(cartItem -> cartItem.equals(item))
                .findFirst()
                .orElse(null);
        
        if (existingItem != null) {
            existingItem.addQuantity(item.getQuantity());
        } else {
            items.add(item);
        }
        calculateTotals();
    }

    // Remove item from order
    public boolean removeItem(int menuItemId) {
        boolean removed = items.removeIf(item -> item.getMenuItem().getId() == menuItemId);
        if (removed) {
            calculateTotals();
        }
        return removed;
    }

    // Update item quantity
    public boolean updateItemQuantity(int menuItemId, int newQuantity) {
        CartItem item = items.stream()
                .filter(cartItem -> cartItem.getMenuItem().getId() == menuItemId)
                .findFirst()
                .orElse(null);
        
        if (item != null) {
            if (newQuantity <= 0) {
                return removeItem(menuItemId);
            } else {
                item.updateQuantity(newQuantity);
                calculateTotals();
                return true;
            }
        }
        return false;
    }

    // Get total item count
    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<CartItem> items) {
        this.items = new ArrayList<>(items);
        calculateTotals();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public double getTax() {
        return tax;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
        calculateTotals(); // Recalculate because delivery fee changes
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public LocalDateTime getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return String.format("Order{orderId='%s', customerId='%s', status=%s, totalAmount=%.2f, itemCount=%d}",
                orderId, customerId, status, totalAmount, getTotalItemCount());
    }

    // Method to display order in a formatted way
    public String toDisplayString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("Order ID: %s\n", orderId));
        sb.append(String.format("Customer ID: %s\n", customerId));
        sb.append(String.format("Status: %s\n", status));
        sb.append(String.format("Order Time: %s\n", orderTime.format(formatter)));
        sb.append(String.format("Estimated Delivery: %s\n", estimatedDeliveryTime.format(formatter)));
        sb.append(String.format("Delivery Type: %s\n", deliveryType != null ? deliveryType : "standard"));
        
        if (deliveryAddress != null && !deliveryAddress.isEmpty()) {
            sb.append(String.format("Delivery Address: %s\n", deliveryAddress));
        }
        
        if (customerPhone != null && !customerPhone.isEmpty()) {
            sb.append(String.format("Customer Phone: %s\n", customerPhone));
        }
        
        sb.append("\nItems:\n");
        for (CartItem item : items) {
            sb.append("  ").append(item.toDisplayString()).append("\n");
        }
        
        sb.append(String.format("\nSubtotal: $%.2f\n", subtotal));
        sb.append(String.format("Delivery Fee: $%.2f\n", deliveryFee));
        sb.append(String.format("Tax: $%.2f\n", tax));
        sb.append(String.format("Total Amount: $%.2f\n", totalAmount));
        
        return sb.toString();
    }
}
