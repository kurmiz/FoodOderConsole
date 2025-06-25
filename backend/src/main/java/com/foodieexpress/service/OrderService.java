package com.foodieexpress.service;

import com.foodieexpress.model.Cart;
import com.foodieexpress.model.Order;
import com.foodieexpress.model.Order.OrderStatus;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing orders
 */
public class OrderService {
    private final List<Order> orders;
    private final CartService cartService;
    private int orderCounter;

    public OrderService(CartService cartService) {
        this.orders = new ArrayList<>();
        this.cartService = cartService;
        this.orderCounter = 1000; // Start order IDs from 1000
    }

    // Create order from cart
    public Order createOrderFromCart(String customerId, String deliveryType, String deliveryAddress, String customerPhone) {
        Cart cart = cartService.getCart(customerId);
        
        if (cart == null || cart.isEmpty()) {
            return null;
        }
        
        // Validate cart before creating order
        if (!cartService.validateCart(customerId)) {
            return null;
        }
        
        // Generate order ID
        String orderId = String.valueOf(orderCounter++);
        
        // Convert cart to order
        Order order = cart.convertToOrder(orderId);
        if (order == null) {
            return null;
        }
        
        // Set order details
        order.setDeliveryType(deliveryType != null ? deliveryType : "standard");
        order.setDeliveryAddress(deliveryAddress);
        order.setCustomerPhone(customerPhone);
        order.setStatus(OrderStatus.CONFIRMED);
        
        // Recalculate totals with delivery type
        order.calculateTotals();
        
        // Add to orders list
        orders.add(order);
        
        // Clear customer's cart
        cartService.clearCart(customerId);
        
        return order;
    }

    // Get order by ID
    public Optional<Order> getOrderById(String orderId) {
        return orders.stream()
                .filter(order -> order.getOrderId().equals(orderId))
                .findFirst();
    }

    // Get all orders
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    // Get orders by customer
    public List<Order> getOrdersByCustomer(String customerId) {
        return orders.stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .sorted((o1, o2) -> o2.getOrderTime().compareTo(o1.getOrderTime())) // Latest first
                .collect(Collectors.toList());
    }

    // Get orders by status
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orders.stream()
                .filter(order -> order.getStatus() == status)
                .collect(Collectors.toList());
    }

    // Get recent orders (last 24 hours)
    public List<Order> getRecentOrders() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return orders.stream()
                .filter(order -> order.getOrderTime().isAfter(yesterday))
                .sorted((o1, o2) -> o2.getOrderTime().compareTo(o1.getOrderTime()))
                .collect(Collectors.toList());
    }

    // Update order status
    public boolean updateOrderStatus(String orderId, OrderStatus newStatus) {
        Optional<Order> order = getOrderById(orderId);
        if (order.isPresent()) {
            order.get().setStatus(newStatus);
            return true;
        }
        return false;
    }

    // Cancel order
    public boolean cancelOrder(String orderId) {
        Optional<Order> order = getOrderById(orderId);
        if (order.isPresent()) {
            Order orderObj = order.get();
            // Can only cancel if not yet delivered
            if (orderObj.getStatus() != OrderStatus.DELIVERED) {
                orderObj.setStatus(OrderStatus.CANCELLED);
                return true;
            }
        }
        return false;
    }

    // Get order statistics
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalOrders", orders.size());
        
        // Orders by status
        Map<OrderStatus, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        stats.put("ordersByStatus", ordersByStatus);
        
        // Total revenue
        double totalRevenue = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount)
                .sum();
        stats.put("totalRevenue", totalRevenue);
        
        // Average order value
        OptionalDouble avgOrderValue = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount)
                .average();
        stats.put("averageOrderValue", avgOrderValue.orElse(0.0));
        
        // Orders today
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long ordersToday = orders.stream()
                .filter(order -> order.getOrderTime().isAfter(startOfDay))
                .count();
        stats.put("ordersToday", ordersToday);
        
        // Revenue today
        double revenueToday = orders.stream()
                .filter(order -> order.getOrderTime().isAfter(startOfDay) && 
                                order.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount)
                .sum();
        stats.put("revenueToday", revenueToday);
        
        return stats;
    }

    // Display order details
    public void displayOrder(String orderId) {
        Optional<Order> order = getOrderById(orderId);
        
        if (order.isPresent()) {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("                           ORDER DETAILS");
            System.out.println("=".repeat(80));
            System.out.println(order.get().toDisplayString());
            System.out.println("=".repeat(80));
        } else {
            System.out.println("\nOrder not found: " + orderId);
        }
    }

    // Display all orders
    public void displayAllOrders() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                           ALL ORDERS");
        System.out.println("=".repeat(80));
        
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            System.out.println("=".repeat(80));
            return;
        }
        
        // Sort by order time (latest first)
        List<Order> sortedOrders = orders.stream()
                .sorted((o1, o2) -> o2.getOrderTime().compareTo(o1.getOrderTime()))
                .collect(Collectors.toList());
        
        for (Order order : sortedOrders) {
            System.out.println(order.toDisplayString());
            System.out.println("-".repeat(80));
        }
        
        System.out.println("=".repeat(80));
    }

    // Display orders by customer
    public void displayOrdersByCustomer(String customerId) {
        List<Order> customerOrders = getOrdersByCustomer(customerId);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                    ORDERS FOR CUSTOMER: " + customerId);
        System.out.println("=".repeat(80));
        
        if (customerOrders.isEmpty()) {
            System.out.println("No orders found for customer: " + customerId);
            System.out.println("=".repeat(80));
            return;
        }
        
        for (Order order : customerOrders) {
            System.out.println(order.toDisplayString());
            System.out.println("-".repeat(80));
        }
        
        System.out.println("=".repeat(80));
    }

    // Display orders by status
    public void displayOrdersByStatus(OrderStatus status) {
        List<Order> statusOrders = getOrdersByStatus(status);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                    ORDERS WITH STATUS: " + status);
        System.out.println("=".repeat(80));
        
        if (statusOrders.isEmpty()) {
            System.out.println("No orders found with status: " + status);
            System.out.println("=".repeat(80));
            return;
        }
        
        for (Order order : statusOrders) {
            System.out.println(order.toDisplayString());
            System.out.println("-".repeat(80));
        }
        
        System.out.println("=".repeat(80));
    }

    // Display order statistics
    public void displayOrderStatistics() {
        Map<String, Object> stats = getOrderStatistics();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                    ORDER STATISTICS");
        System.out.println("=".repeat(60));
        System.out.println("Total Orders: " + stats.get("totalOrders"));
        System.out.println("Orders Today: " + stats.get("ordersToday"));
        System.out.printf("Total Revenue: $%.2f%n", (Double) stats.get("totalRevenue"));
        System.out.printf("Revenue Today: $%.2f%n", (Double) stats.get("revenueToday"));
        System.out.printf("Average Order Value: $%.2f%n", (Double) stats.get("averageOrderValue"));
        
        System.out.println("\nOrders by Status:");
        @SuppressWarnings("unchecked")
        Map<OrderStatus, Long> ordersByStatus = (Map<OrderStatus, Long>) stats.get("ordersByStatus");
        for (OrderStatus status : OrderStatus.values()) {
            long count = ordersByStatus.getOrDefault(status, 0L);
            System.out.println("  " + status + ": " + count);
        }
        
        System.out.println("=".repeat(60));
    }

    // Get pending orders (for kitchen/preparation)
    public List<Order> getPendingOrders() {
        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.CONFIRMED || 
                                order.getStatus() == OrderStatus.PREPARING)
                .sorted(Comparator.comparing(Order::getOrderTime))
                .collect(Collectors.toList());
    }

    // Display pending orders
    public void displayPendingOrders() {
        List<Order> pendingOrders = getPendingOrders();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                         PENDING ORDERS");
        System.out.println("=".repeat(80));
        
        if (pendingOrders.isEmpty()) {
            System.out.println("No pending orders.");
            System.out.println("=".repeat(80));
            return;
        }
        
        for (Order order : pendingOrders) {
            System.out.println(order.toDisplayString());
            System.out.println("-".repeat(80));
        }
        
        System.out.println("=".repeat(80));
    }
}
