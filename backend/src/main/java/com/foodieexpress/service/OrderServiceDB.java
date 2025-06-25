package com.foodieexpress.service;

import com.foodieexpress.model.Order;
import com.foodieexpress.model.CartItem;
import com.foodieexpress.database.DatabaseManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Database-backed service class for managing orders with real-time persistence
 */
public class OrderServiceDB {
    private static final String COLLECTION = "orders";
    private final CartServiceDB cartService;

    public OrderServiceDB(CartServiceDB cartService) {
        this.cartService = cartService;
        DatabaseManager.initialize();
    }

    // Create order from cart
    public Order createOrderFromCart(String customerId, String deliveryType, String address, String phone) {
        List<CartItem> cartItems = cartService.getCartItems(customerId);
        
        if (cartItems.isEmpty()) {
            System.out.println("❌ Cannot create order: Cart is empty for customer " + customerId);
            return null;
        }

        // Calculate totals
        double subtotal = cartService.getCartSubtotal(customerId);
        double deliveryFee = "express".equals(deliveryType) ? 5.99 : 2.99;
        double tax = subtotal * 0.08; // 8% tax
        double totalAmount = subtotal + deliveryFee + tax;

        // Generate order ID
        String orderId = generateOrderId();

        // Create order document
        Map<String, Object> orderDoc = new HashMap<>();
        orderDoc.put("orderId", orderId);
        orderDoc.put("customerId", customerId);
        orderDoc.put("status", "PENDING");
        orderDoc.put("orderTime", LocalDateTime.now().toString());
        orderDoc.put("deliveryType", deliveryType);
        orderDoc.put("address", address);
        orderDoc.put("phone", phone);
        orderDoc.put("subtotal", subtotal);
        orderDoc.put("deliveryFee", deliveryFee);
        orderDoc.put("tax", tax);
        orderDoc.put("totalAmount", totalAmount);
        orderDoc.put("estimatedDeliveryTime", calculateEstimatedDelivery(deliveryType));
        
        // Store cart items in order
        List<Map<String, Object>> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            Map<String, Object> orderItem = new HashMap<>();
            orderItem.put("itemId", item.getMenuItem().getId());
            orderItem.put("itemName", item.getMenuItem().getName());
            orderItem.put("itemPrice", item.getMenuItem().getPrice());
            orderItem.put("quantity", item.getQuantity());
            orderItem.put("specialInstructions", item.getSpecialInstructions());
            orderItem.put("totalPrice", item.getTotalPrice());
            orderItems.add(orderItem);
        }
        orderDoc.put("items", orderItems);
        orderDoc.put("totalItemCount", cartItems.stream().mapToInt(CartItem::getQuantity).sum());

        // Save order to database
        String id = DatabaseManager.insert(COLLECTION, orderDoc);
        
        if (id != null) {
            // Clear the cart after successful order creation
            cartService.clearCart(customerId);
            
            // Create Order object
            Order order = documentToOrder(orderDoc);
            
            System.out.println("🎉 Order created successfully!");
            System.out.println("📦 Order ID: " + orderId);
            System.out.printf("💰 Total Amount: $%.2f%n", totalAmount);
            System.out.println("🚚 Delivery Type: " + deliveryType);
            System.out.println("📍 Address: " + address);
            
            return order;
        }
        
        return null;
    }

    // Get order by ID
    public Optional<Order> getOrderById(String orderId) {
        List<Map<String, Object>> orders = DatabaseManager.findByField(COLLECTION, "orderId", orderId);
        if (!orders.isEmpty()) {
            return Optional.of(documentToOrder(orders.get(0)));
        }
        return Optional.empty();
    }

    // Get orders by customer
    public List<Order> getOrdersByCustomer(String customerId) {
        List<Map<String, Object>> orderDocs = DatabaseManager.findByField(COLLECTION, "customerId", customerId);
        return orderDocs.stream()
                .map(this::documentToOrder)
                .sorted((o1, o2) -> o2.getOrderTime().compareTo(o1.getOrderTime())) // Latest first
                .collect(Collectors.toList());
    }

    // Get orders by status
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        List<Map<String, Object>> orderDocs = DatabaseManager.findByField(COLLECTION, "status", status.toString());
        return orderDocs.stream()
                .map(this::documentToOrder)
                .sorted((o1, o2) -> o2.getOrderTime().compareTo(o1.getOrderTime()))
                .collect(Collectors.toList());
    }

    // Get all orders
    public List<Order> getAllOrders() {
        List<Map<String, Object>> orderDocs = DatabaseManager.findAll(COLLECTION);
        return orderDocs.stream()
                .map(this::documentToOrder)
                .sorted((o1, o2) -> o2.getOrderTime().compareTo(o1.getOrderTime()))
                .collect(Collectors.toList());
    }

    // Update order status
    public boolean updateOrderStatus(String orderId, Order.OrderStatus newStatus) {
        List<Map<String, Object>> orders = DatabaseManager.findByField(COLLECTION, "orderId", orderId);
        
        if (!orders.isEmpty()) {
            Map<String, Object> orderDoc = orders.get(0);
            String id = orderDoc.get("_id").toString();
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", newStatus.toString());
            updates.put("statusUpdatedAt", LocalDateTime.now().toString());
            
            boolean updated = DatabaseManager.update(COLLECTION, id, updates);
            if (updated) {
                System.out.println("📋 Order status updated: " + orderId + " → " + newStatus);
                return true;
            }
        }
        
        return false;
    }

    // Cancel order
    public boolean cancelOrder(String orderId) {
        Optional<Order> order = getOrderById(orderId);
        if (order.isPresent()) {
            Order.OrderStatus currentStatus = order.get().getStatus();
            
            // Can only cancel pending or preparing orders
            if (currentStatus == Order.OrderStatus.PENDING || currentStatus == Order.OrderStatus.PREPARING) {
                return updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
            } else {
                System.out.println("❌ Cannot cancel order " + orderId + " - Status: " + currentStatus);
                return false;
            }
        }
        return false;
    }

    // Get recent orders (last 24 hours)
    public List<Order> getRecentOrders() {
        List<Order> allOrders = getAllOrders();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        
        return allOrders.stream()
                .filter(order -> order.getOrderTime().isAfter(yesterday))
                .collect(Collectors.toList());
    }

    // Display order
    public void displayOrder(String orderId) {
        Optional<Order> order = getOrderById(orderId);
        
        if (!order.isPresent()) {
            System.out.println("❌ Order not found: " + orderId);
            return;
        }
        
        Order o = order.get();
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                        ORDER DETAILS");
        System.out.println("=".repeat(70));
        System.out.println("📦 Order ID: " + o.getOrderId());
        System.out.println("👤 Customer: " + o.getCustomerId());
        System.out.println("📅 Order Time: " + o.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("📋 Status: " + getStatusEmoji(o.getStatus()) + " " + o.getStatus());
        System.out.println("🚚 Delivery: " + o.getDeliveryType());
        System.out.println("📍 Address: " + o.getDeliveryAddress());
        System.out.println("📞 Phone: " + o.getCustomerPhone());
        
        if (o.getEstimatedDeliveryTime() != null) {
            System.out.println("⏰ Estimated Delivery: " + o.getEstimatedDeliveryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        System.out.println("\n📋 ORDER ITEMS:");
        System.out.println("-".repeat(70));
        
        // Note: In a real implementation, we'd store and retrieve the actual cart items
        // For now, we'll show basic order info
        System.out.printf("📊 Total Items: %d%n", o.getTotalItemCount());
        System.out.printf("💰 Subtotal: $%.2f%n", o.getSubtotal());
        System.out.printf("🚚 Delivery Fee: $%.2f%n", o.getDeliveryFee());
        System.out.printf("💸 Tax: $%.2f%n", o.getTax());
        System.out.println("-".repeat(70));
        System.out.printf("💳 TOTAL: $%.2f%n", o.getTotalAmount());
        System.out.println("=".repeat(70));
    }

    // Display orders by customer
    public void displayOrdersByCustomer(String customerId) {
        List<Order> orders = getOrdersByCustomer(customerId);
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                    ORDERS FOR " + customerId.toUpperCase());
        System.out.println("=".repeat(70));
        
        if (orders.isEmpty()) {
            System.out.println("📦 No orders found for customer: " + customerId);
            System.out.println("=".repeat(70));
            return;
        }
        
        for (Order order : orders) {
            System.out.printf("📦 %s | %s | %s $%.2f | %s%n",
                    order.getOrderId(),
                    order.getOrderTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                    getStatusEmoji(order.getStatus()) + " " + order.getStatus(),
                    order.getTotalAmount(),
                    order.getDeliveryType());
        }
        
        System.out.println("=".repeat(70));
        System.out.println("Total Orders: " + orders.size());
        System.out.println("=".repeat(70));
    }

    // Display all orders (admin)
    public void displayAllOrders() {
        List<Order> orders = getAllOrders();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                           ALL ORDERS");
        System.out.println("=".repeat(80));
        
        if (orders.isEmpty()) {
            System.out.println("📦 No orders in the system.");
            System.out.println("=".repeat(80));
            return;
        }
        
        System.out.printf("%-12s %-15s %-12s %-10s %-8s %-10s%n",
                "Order ID", "Customer", "Time", "Status", "Total", "Delivery");
        System.out.println("-".repeat(80));
        
        for (Order order : orders) {
            System.out.printf("%-12s %-15s %-12s %-10s $%-7.2f %-10s%n",
                    order.getOrderId(),
                    order.getCustomerId(),
                    order.getOrderTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                    order.getStatus(),
                    order.getTotalAmount(),
                    order.getDeliveryType());
        }
        
        System.out.println("=".repeat(80));
        System.out.println("Total Orders: " + orders.size());
        System.out.println("=".repeat(80));
    }

    // Display pending orders
    public void displayPendingOrders() {
        List<Order> pendingOrders = getOrdersByStatus(Order.OrderStatus.PENDING);
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                      PENDING ORDERS");
        System.out.println("=".repeat(70));
        
        if (pendingOrders.isEmpty()) {
            System.out.println("✅ No pending orders.");
            System.out.println("=".repeat(70));
            return;
        }
        
        for (Order order : pendingOrders) {
            System.out.printf("📦 %s | %s | $%.2f | %s%n",
                    order.getOrderId(),
                    order.getCustomerId(),
                    order.getTotalAmount(),
                    order.getDeliveryType());
        }
        
        System.out.println("=".repeat(70));
        System.out.println("Pending Orders: " + pendingOrders.size());
        System.out.println("=".repeat(70));
    }

    // Display orders by status
    public void displayOrdersByStatus(Order.OrderStatus status) {
        List<Order> orders = getOrdersByStatus(status);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("                    ORDERS BY STATUS: " + status);
        System.out.println("=".repeat(70));

        if (orders.isEmpty()) {
            System.out.println("📦 No orders found with status: " + status);
            System.out.println("=".repeat(70));
            return;
        }

        System.out.printf("%-12s %-15s %-12s %-8s %-10s%n",
                "Order ID", "Customer", "Time", "Total", "Delivery");
        System.out.println("-".repeat(70));

        for (Order order : orders) {
            System.out.printf("%-12s %-15s %-12s $%-7.2f %-10s%n",
                    order.getOrderId(),
                    order.getCustomerId(),
                    order.getOrderTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                    order.getTotalAmount(),
                    order.getDeliveryType());
        }

        System.out.println("=".repeat(70));
        System.out.println("Total Orders with status " + status + ": " + orders.size());
        System.out.println("=".repeat(70));
    }

    // Display order statistics
    public void displayOrderStatistics() {
        List<Order> allOrders = getAllOrders();

        if (allOrders.isEmpty()) {
            System.out.println("📊 No order data available.");
            return;
        }

        // Group by status
        Map<Order.OrderStatus, Long> ordersByStatus = allOrders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        // Calculate totals
        double totalRevenue = allOrders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        double avgOrderValue = totalRevenue / allOrders.size();

        System.out.println("📊 ORDER STATISTICS:");
        System.out.println("   Total Orders: " + allOrders.size());
        System.out.printf("   Total Revenue: $%.2f%n", totalRevenue);
        System.out.printf("   Average Order Value: $%.2f%n", avgOrderValue);

        System.out.println("   Orders by Status:");
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            long count = ordersByStatus.getOrDefault(status, 0L);
            System.out.println("     " + getStatusEmoji(status) + " " + status + ": " + count);
        }

        // Recent orders
        List<Order> recentOrders = getRecentOrders();
        System.out.println("   Recent Orders (24h): " + recentOrders.size());
    }

    // Helper methods
    private String generateOrderId() {
        return "ORD" + System.currentTimeMillis() % 1000000;
    }

    private LocalDateTime calculateEstimatedDelivery(String deliveryType) {
        int minutes = "express".equals(deliveryType) ? 20 : 35;
        return LocalDateTime.now().plusMinutes(minutes);
    }

    private String getStatusEmoji(Order.OrderStatus status) {
        switch (status) {
            case PENDING: return "⏳";
            case PREPARING: return "👨‍🍳";
            case ON_THE_WAY: return "🚚";
            case DELIVERED: return "✅";
            case CANCELLED: return "❌";
            default: return "📦";
        }
    }

    private Order documentToOrder(Map<String, Object> doc) {
        String orderId = (String) doc.get("orderId");
        String customerId = (String) doc.get("customerId");
        Order.OrderStatus status = Order.OrderStatus.valueOf((String) doc.get("status"));
        LocalDateTime orderTime = LocalDateTime.parse((String) doc.get("orderTime"));
        String deliveryType = (String) doc.get("deliveryType");
        String address = (String) doc.get("address");
        String phone = (String) doc.get("phone");
        // Note: We store financial data in database but Order class calculates them
        // double subtotal = ((Number) doc.get("subtotal")).doubleValue();
        // double deliveryFee = ((Number) doc.get("deliveryFee")).doubleValue();
        // double tax = ((Number) doc.get("tax")).doubleValue();
        // double totalAmount = ((Number) doc.get("totalAmount")).doubleValue();
        // int totalItemCount = ((Number) doc.get("totalItemCount")).intValue();

        // LocalDateTime estimatedDelivery = null;
        // if (doc.containsKey("estimatedDeliveryTime")) {
        //     estimatedDelivery = LocalDateTime.parse((String) doc.get("estimatedDeliveryTime"));
        // }

        // Create order using existing constructor and set available properties
        Order order = new Order(orderId, customerId);
        order.setStatus(status);
        order.setOrderTime(orderTime);
        order.setDeliveryType(deliveryType);
        order.setDeliveryAddress(address);
        order.setCustomerPhone(phone);

        // Note: Financial fields (subtotal, tax, etc.) are calculated by the Order class
        // We store them in the database but the Order object calculates them based on items

        return order;
    }

    // Helper method to get total item count from order
    public int getTotalItemCount(Order order) {
        return order.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
    }
}
