package com.foodieexpress;

import com.foodieexpress.server.HttpServerDB;
import com.foodieexpress.service.MenuServiceDB;
import com.foodieexpress.service.CartServiceDB;
import com.foodieexpress.service.OrderServiceDB;
import com.foodieexpress.database.DatabaseManager;
import com.foodieexpress.model.MenuItem;
import com.foodieexpress.model.Order;
import com.foodieexpress.model.Order.OrderStatus;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Integrated application that runs both web server and CLI
 */
public class FoodieExpressIntegratedApp {
    private final MenuServiceDB menuService;
    private final CartServiceDB cartService;
    private final OrderServiceDB orderService;
    private final HttpServerDB webServer;
    private final Scanner scanner;
    private String currentCustomerId;
    private boolean serverRunning = false;

    public FoodieExpressIntegratedApp() throws IOException {
        // Initialize database first
        DatabaseManager.initialize();

        // Initialize services with database backing
        this.menuService = new MenuServiceDB();
        this.cartService = new CartServiceDB(menuService);
        this.orderService = new OrderServiceDB(cartService);
        this.webServer = new HttpServerDB(8080, menuService, cartService, orderService);
        this.scanner = new Scanner(System.in);
        this.currentCustomerId = "cli-customer-1";

        System.out.println("🚀 FoodieExpress Integrated System initialized with real-time database!");
    }

    public static void main(String[] args) {
        try {
            FoodieExpressIntegratedApp app = new FoodieExpressIntegratedApp();
            app.run();
        } catch (IOException e) {
            System.err.println("Failed to start application: " + e.getMessage());
        }
    }

    public void run() {
        displayWelcome();
        
        while (true) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    toggleWebServer();
                    break;
                case 2:
                    handleMenuOperations();
                    break;
                case 3:
                    handleCartOperations();
                    break;
                case 4:
                    handleOrderOperations();
                    break;
                case 5:
                    handleCustomerOperations();
                    break;
                case 6:
                    handleAdminOperations();
                    break;
                case 7:
                    displaySystemStatus();
                    break;
                case 8:
                    System.out.println("\n🛑 Shutting down FoodieExpress...");
                    if (serverRunning) {
                        webServer.stop();
                    }
                    System.out.println("Thank you for using FoodieExpress! Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void displayWelcome() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                🍕 FOODIE EXPRESS INTEGRATED SYSTEM 🍕");
        System.out.println("              Frontend + Backend + CLI Administration");
        System.out.println("=".repeat(80));
        System.out.println("Current CLI Customer: " + currentCustomerId);
        System.out.println("Web Server Status: " + (serverRunning ? "🟢 RUNNING" : "🔴 STOPPED"));
        System.out.println("=".repeat(80));
    }

    private void displayMainMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                    INTEGRATED MAIN MENU");
        System.out.println("=".repeat(60));
        System.out.println("1. " + (serverRunning ? "🛑 Stop Web Server" : "🚀 Start Web Server"));
        System.out.println("2. 📋 Menu Operations (CLI)");
        System.out.println("3. 🛒 Cart Operations (CLI)");
        System.out.println("4. 📦 Order Operations (CLI)");
        System.out.println("5. 👤 Customer Operations (CLI)");
        System.out.println("6. 🔧 Admin Operations");
        System.out.println("7. 📊 System Status & Statistics");
        System.out.println("8. 🚪 Exit");
        System.out.println("=".repeat(60));
        if (serverRunning) {
            System.out.println("🌐 Frontend URL: http://localhost:8080");
            System.out.println("🔗 API Base URL: http://localhost:8080/api");
        }
        System.out.println("=".repeat(60));
    }

    private void toggleWebServer() {
        if (serverRunning) {
            webServer.stop();
            serverRunning = false;
            System.out.println("\n🛑 Web server stopped.");
        } else {
            webServer.start();
            serverRunning = true;
            System.out.println("\n🎉 Web server started successfully!");
            System.out.println("📱 You can now access the frontend at: http://localhost:8080");
            System.out.println("🔗 API endpoints available at: http://localhost:8080/api");
            System.out.println("\n💡 Tip: Open the frontend in your browser to test the integration!");
        }
    }

    private void displaySystemStatus() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                        SYSTEM STATUS & STATISTICS");
        System.out.println("=".repeat(80));
        
        // Server status
        System.out.println("🌐 Web Server: " + (serverRunning ? "🟢 RUNNING on port 8080" : "🔴 STOPPED"));
        System.out.println("👤 Current CLI Customer: " + currentCustomerId);
        
        // Menu statistics
        System.out.println("\n📋 MENU STATISTICS:");
        List<MenuItem> allItems = menuService.getAllMenuItems();
        List<MenuItem> availableItems = menuService.getAvailableMenuItems();
        System.out.println("   Total Items: " + allItems.size());
        System.out.println("   Available Items: " + availableItems.size());
        System.out.println("   Categories: " + menuService.getAllCategories().size());
        
        // Cart statistics
        System.out.println("\n🛒 CART STATISTICS:");
        cartService.displayCartStatistics();
        
        // Order statistics
        System.out.println("\n📦 ORDER STATISTICS:");
        orderService.displayOrderStatistics();
        
        // Recent activity
        System.out.println("\n📈 RECENT ACTIVITY:");
        List<Order> recentOrders = orderService.getRecentOrders();
        System.out.println("   Orders in last 24h: " + recentOrders.size());
        
        if (!recentOrders.isEmpty()) {
            System.out.println("   Latest order: #" + recentOrders.get(0).getOrderId() + 
                             " (" + recentOrders.get(0).getStatus() + ")");
        }
        
        System.out.println("\n💡 INTEGRATION STATUS:");
        System.out.println("   ✅ CLI Interface: Active");
        System.out.println("   " + (serverRunning ? "✅" : "❌") + " Web Server: " + 
                          (serverRunning ? "Active" : "Inactive"));
        System.out.println("   ✅ REST API: " + (serverRunning ? "Available" : "Unavailable"));
        System.out.println("   ✅ Real-time Data Sync: Active");
        
        System.out.println("=".repeat(80));
    }

    // Menu operations (same as original CLI)
    private void handleMenuOperations() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("              MENU OPERATIONS (CLI)");
            System.out.println("=".repeat(50));
            System.out.println("1. View All Menu Items");
            System.out.println("2. View Menu by Category");
            System.out.println("3. Search Menu Items");
            System.out.println("4. View Menu Statistics");
            System.out.println("5. Add Item to Cart");
            System.out.println("6. Back to Main Menu");
            System.out.println("=".repeat(50));
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    menuService.displayAllMenuItems();
                    break;
                case 2:
                    viewMenuByCategory();
                    break;
                case 3:
                    searchMenuItems();
                    break;
                case 4:
                    menuService.displayMenuStatistics();
                    break;
                case 5:
                    addItemToCartFromMenu();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Cart operations (same as original CLI)
    private void handleCartOperations() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("              CART OPERATIONS (CLI)");
            System.out.println("=".repeat(50));
            System.out.println("1. View Cart");
            System.out.println("2. Add Item to Cart");
            System.out.println("3. Update Item Quantity");
            System.out.println("4. Remove Item from Cart");
            System.out.println("5. Clear Cart");
            System.out.println("6. Proceed to Checkout");
            System.out.println("7. Back to Main Menu");
            System.out.println("=".repeat(50));
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    cartService.displayCart(currentCustomerId);
                    break;
                case 2:
                    addItemToCart();
                    break;
                case 3:
                    updateCartItemQuantity();
                    break;
                case 4:
                    removeItemFromCart();
                    break;
                case 5:
                    clearCart();
                    break;
                case 6:
                    proceedToCheckout();
                    break;
                case 7:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Order operations (same as original CLI)
    private void handleOrderOperations() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("             ORDER OPERATIONS (CLI)");
            System.out.println("=".repeat(50));
            System.out.println("1. View My Orders");
            System.out.println("2. View Order Details");
            System.out.println("3. Cancel Order");
            System.out.println("4. Track Order Status");
            System.out.println("5. Back to Main Menu");
            System.out.println("=".repeat(50));
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    orderService.displayOrdersByCustomer(currentCustomerId);
                    break;
                case 2:
                    viewOrderDetails();
                    break;
                case 3:
                    cancelOrder();
                    break;
                case 4:
                    trackOrderStatus();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Customer operations
    private void handleCustomerOperations() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("           CUSTOMER OPERATIONS (CLI)");
            System.out.println("=".repeat(50));
            System.out.println("1. Switch Customer");
            System.out.println("2. View Current Customer Info");
            System.out.println("3. Quick Order (Popular Items)");
            System.out.println("4. Back to Main Menu");
            System.out.println("=".repeat(50));
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    switchCustomer();
                    break;
                case 2:
                    viewCurrentCustomerInfo();
                    break;
                case 3:
                    quickOrder();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Admin operations (enhanced with web monitoring)
    private void handleAdminOperations() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("             ADMIN OPERATIONS");
            System.out.println("=".repeat(50));
            System.out.println("1. View All Orders");
            System.out.println("2. View Orders by Status");
            System.out.println("3. Update Order Status");
            System.out.println("4. View Pending Orders");
            System.out.println("5. View All Customer Carts");
            System.out.println("6. Monitor Web Activity");
            System.out.println("7. Add Menu Item");
            System.out.println("8. Toggle Item Availability");
            System.out.println("9. System Statistics");
            System.out.println("10. Back to Main Menu");
            System.out.println("=".repeat(50));
            
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    orderService.displayAllOrders();
                    break;
                case 2:
                    viewOrdersByStatus();
                    break;
                case 3:
                    updateOrderStatus();
                    break;
                case 4:
                    orderService.displayPendingOrders();
                    break;
                case 5:
                    cartService.displayAllCarts();
                    break;
                case 6:
                    monitorWebActivity();
                    break;
                case 7:
                    addMenuItem();
                    break;
                case 8:
                    toggleItemAvailability();
                    break;
                case 9:
                    displaySystemStatus();
                    break;
                case 10:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void monitorWebActivity() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                  WEB ACTIVITY MONITOR");
        System.out.println("=".repeat(60));
        System.out.println("Server Status: " + (serverRunning ? "🟢 RUNNING" : "🔴 STOPPED"));
        
        if (serverRunning) {
            System.out.println("Port: 8080");
            System.out.println("Frontend URL: http://localhost:8080");
            System.out.println("API Endpoints:");
            System.out.println("  📋 GET /api/menu - Get menu items");
            System.out.println("  🛒 GET /api/cart - Get cart contents");
            System.out.println("  ➕ POST /api/cart - Add item to cart");
            System.out.println("  📦 GET /api/orders - Get customer orders");
            System.out.println("  🎯 POST /api/orders - Create new order");
            
            System.out.println("\n💡 Real-time Integration:");
            System.out.println("  ✅ All web orders appear in CLI immediately");
            System.out.println("  ✅ CLI changes reflect in web interface");
            System.out.println("  ✅ Shared data storage (in-memory)");
        } else {
            System.out.println("\n⚠️  Web server is not running.");
            System.out.println("Start the web server to enable frontend integration.");
        }
        
        System.out.println("=".repeat(60));
    }

    // Helper methods (same as original CLI app)
    private void viewMenuByCategory() {
        System.out.println("\nAvailable categories:");
        menuService.getAllCategories().forEach(category -> 
            System.out.println("- " + category));
        
        String category = getStringInput("Enter category: ");
        List<MenuItem> items = menuService.getMenuItemsByCategory(category);
        
        if (items.isEmpty()) {
            System.out.println("No items found in category: " + category);
        } else {
            System.out.println("\n" + category.toUpperCase() + " ITEMS:");
            System.out.println("-".repeat(50));
            items.forEach(item -> {
                System.out.println(item.toDisplayString());
                System.out.println();
            });
        }
    }

    private void searchMenuItems() {
        String searchTerm = getStringInput("Enter search term: ");
        List<MenuItem> items = menuService.searchMenuItems(searchTerm);
        
        if (items.isEmpty()) {
            System.out.println("No items found matching: " + searchTerm);
        } else {
            System.out.println("\nSEARCH RESULTS:");
            System.out.println("-".repeat(50));
            items.forEach(item -> {
                System.out.println(item.toDisplayString());
                System.out.println();
            });
        }
    }

    private void addItemToCartFromMenu() {
        int itemId = getIntInput("Enter item ID to add to cart: ");
        int quantity = getIntInput("Enter quantity: ");
        String instructions = getStringInput("Enter special instructions (optional): ");
        
        if (cartService.addItemToCart(currentCustomerId, itemId, quantity, instructions)) {
            System.out.println("✅ Item added to cart successfully!");
            System.out.println("Cart summary: " + cartService.getCartSummary(currentCustomerId));
        } else {
            System.out.println("❌ Failed to add item to cart. Please check item ID and availability.");
        }
    }

    private void addItemToCart() {
        menuService.displayAllMenuItems();
        addItemToCartFromMenu();
    }

    private void updateCartItemQuantity() {
        cartService.displayCart(currentCustomerId);
        
        if (cartService.isCartEmpty(currentCustomerId)) {
            return;
        }
        
        int itemId = getIntInput("Enter item ID to update: ");
        int newQuantity = getIntInput("Enter new quantity: ");
        
        if (cartService.updateCartItemQuantity(currentCustomerId, itemId, newQuantity)) {
            System.out.println("✅ Item quantity updated successfully!");
            cartService.displayCart(currentCustomerId);
        } else {
            System.out.println("❌ Failed to update item quantity.");
        }
    }

    private void removeItemFromCart() {
        cartService.displayCart(currentCustomerId);
        
        if (cartService.isCartEmpty(currentCustomerId)) {
            return;
        }
        
        int itemId = getIntInput("Enter item ID to remove: ");
        
        if (cartService.removeItemFromCart(currentCustomerId, itemId)) {
            System.out.println("✅ Item removed from cart successfully!");
            cartService.displayCart(currentCustomerId);
        } else {
            System.out.println("❌ Failed to remove item from cart.");
        }
    }

    private void clearCart() {
        String confirm = getStringInput("Are you sure you want to clear your cart? (yes/no): ");
        if ("yes".equalsIgnoreCase(confirm)) {
            cartService.clearCart(currentCustomerId);
            System.out.println("✅ Cart cleared successfully!");
        }
    }

    private void proceedToCheckout() {
        if (cartService.isCartEmpty(currentCustomerId)) {
            System.out.println("Your cart is empty. Add items before checkout.");
            return;
        }
        
        cartService.displayCart(currentCustomerId);
        
        String confirm = getStringInput("Proceed with checkout? (yes/no): ");
        if (!"yes".equalsIgnoreCase(confirm)) {
            return;
        }
        
        String deliveryType = getStringInput("Delivery type (standard/express): ");
        String address = getStringInput("Delivery address: ");
        String phone = getStringInput("Phone number: ");
        
        Order order = orderService.createOrderFromCart(currentCustomerId, deliveryType, address, phone);
        
        if (order != null) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("                🎉 ORDER CONFIRMED! 🎉");
            System.out.println("=".repeat(60));
            System.out.println("Order ID: " + order.getOrderId());
            System.out.printf("Total Amount: $%.2f%n", order.getTotalAmount());
            System.out.println("Estimated Delivery: " + order.getEstimatedDeliveryTime());
            System.out.println("=".repeat(60));
        } else {
            System.out.println("❌ Failed to create order. Please try again.");
        }
    }

    private void viewOrderDetails() {
        String orderId = getStringInput("Enter order ID: ");
        orderService.displayOrder(orderId);
    }

    private void cancelOrder() {
        orderService.displayOrdersByCustomer(currentCustomerId);
        
        String orderId = getStringInput("Enter order ID to cancel: ");
        
        if (orderService.cancelOrder(orderId)) {
            System.out.println("✅ Order cancelled successfully!");
        } else {
            System.out.println("❌ Failed to cancel order. Order may not exist or already delivered.");
        }
    }

    private void trackOrderStatus() {
        String orderId = getStringInput("Enter order ID to track: ");
        Optional<Order> order = orderService.getOrderById(orderId);
        
        if (order.isPresent()) {
            Order orderObj = order.get();
            System.out.println("\n" + "=".repeat(50));
            System.out.println("ORDER TRACKING");
            System.out.println("=".repeat(50));
            System.out.println("Order ID: " + orderObj.getOrderId());
            System.out.println("Status: " + orderObj.getStatus());
            System.out.println("Order Time: " + orderObj.getOrderTime());
            System.out.println("Estimated Delivery: " + orderObj.getEstimatedDeliveryTime());
            System.out.println("=".repeat(50));
        } else {
            System.out.println("Order not found: " + orderId);
        }
    }

    private void switchCustomer() {
        String newCustomerId = getStringInput("Enter customer ID: ");
        this.currentCustomerId = newCustomerId;
        System.out.println("✅ Switched to customer: " + currentCustomerId);
    }

    private void viewCurrentCustomerInfo() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("CURRENT CUSTOMER INFO");
        System.out.println("=".repeat(50));
        System.out.println("Customer ID: " + currentCustomerId);
        System.out.println("Cart Items: " + cartService.getCartItemCount(currentCustomerId));
        System.out.printf("Cart Value: $%.2f%n", cartService.getCartSubtotal(currentCustomerId));
        
        List<Order> customerOrders = orderService.getOrdersByCustomer(currentCustomerId);
        System.out.println("Total Orders: " + customerOrders.size());
        
        if (!customerOrders.isEmpty()) {
            Order lastOrder = customerOrders.get(0);
            System.out.println("Last Order: " + lastOrder.getOrderId() + " (" + lastOrder.getStatus() + ")");
        }
        
        System.out.println("=".repeat(50));
    }

    private void quickOrder() {
        System.out.println("\nQuick Order - Popular Items:");
        System.out.println("1. Pizza");
        System.out.println("2. Burger");
        System.out.println("3. Pasta");
        System.out.println("4. Dessert");
        
        int choice = getIntInput("Select category: ");
        String category = "";
        
        switch (choice) {
            case 1: category = "pizza"; break;
            case 2: category = "burger"; break;
            case 3: category = "pasta"; break;
            case 4: category = "dessert"; break;
            default:
                System.out.println("Invalid choice.");
                return;
        }
        
        if (cartService.quickAddPopularItem(currentCustomerId, category)) {
            System.out.println("✅ Popular " + category + " item added to cart!");
            System.out.println("Cart summary: " + cartService.getCartSummary(currentCustomerId));
        } else {
            System.out.println("❌ Failed to add item to cart.");
        }
    }

    private void viewOrdersByStatus() {
        System.out.println("\nOrder Statuses:");
        for (OrderStatus status : OrderStatus.values()) {
            System.out.println("- " + status);
        }
        
        String statusStr = getStringInput("Enter status: ").toUpperCase();
        
        try {
            OrderStatus status = OrderStatus.valueOf(statusStr);
            orderService.displayOrdersByStatus(status);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status: " + statusStr);
        }
    }

    private void updateOrderStatus() {
        String orderId = getStringInput("Enter order ID: ");
        
        System.out.println("\nAvailable statuses:");
        for (OrderStatus status : OrderStatus.values()) {
            System.out.println("- " + status);
        }
        
        String statusStr = getStringInput("Enter new status: ").toUpperCase();
        
        try {
            OrderStatus newStatus = OrderStatus.valueOf(statusStr);
            if (orderService.updateOrderStatus(orderId, newStatus)) {
                System.out.println("✅ Order status updated successfully!");
            } else {
                System.out.println("❌ Failed to update order status. Order may not exist.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status: " + statusStr);
        }
    }

    private void addMenuItem() {
        String name = getStringInput("Enter item name: ");
        String description = getStringInput("Enter description: ");
        double price = getDoubleInput("Enter price: ");
        String category = getStringInput("Enter category: ");
        String imageUrl = getStringInput("Enter image URL (optional): ");
        
        MenuItem item = menuService.addMenuItem(name, description, price, category, imageUrl);
        System.out.println("✅ Menu item added successfully!");
        System.out.println(item.toDisplayString());
    }

    private void toggleItemAvailability() {
        menuService.displayAllMenuItems();
        
        int itemId = getIntInput("Enter item ID to toggle availability: ");
        
        if (menuService.toggleAvailability(itemId)) {
            System.out.println("✅ Item availability toggled successfully!");
            Optional<MenuItem> item = menuService.getMenuItemById(itemId);
            if (item.isPresent()) {
                System.out.println("New status: " + (item.get().isAvailable() ? "Available" : "Unavailable"));
            }
        } else {
            System.out.println("❌ Failed to toggle availability. Item may not exist.");
        }
    }

    // Utility methods for input
    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
