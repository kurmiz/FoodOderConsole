package com.foodieexpress.service;

import com.foodieexpress.model.CartItem;
import com.foodieexpress.model.MenuItem;
import com.foodieexpress.database.DatabaseManager;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Database-backed service class for managing shopping cart with real-time persistence
 */
public class CartServiceDB {
    private static final String COLLECTION = "cart_items";
    private final MenuServiceDB menuService;

    public CartServiceDB(MenuServiceDB menuService) {
        this.menuService = menuService;
        DatabaseManager.initialize();
    }

    // Add item to cart
    public boolean addItemToCart(String customerId, int itemId, int quantity, String specialInstructions) {
        Optional<MenuItem> menuItem = menuService.getMenuItemById(itemId);
        if (!menuItem.isPresent() || !menuItem.get().isAvailable()) {
            System.out.println("❌ Menu item not available: " + itemId);
            return false;
        }

        // Check if item already exists in cart
        List<Map<String, Object>> existingItems = DatabaseManager.findByField(COLLECTION, "customerId", customerId);
        Map<String, Object> existingCartItem = null;
        
        for (Map<String, Object> item : existingItems) {
            if (Objects.equals(item.get("itemId"), itemId)) {
                existingCartItem = item;
                break;
            }
        }

        if (existingCartItem != null) {
            // Update existing item quantity
            int currentQuantity = ((Number) existingCartItem.get("quantity")).intValue();
            Map<String, Object> updates = new HashMap<>();
            updates.put("quantity", currentQuantity + quantity);
            updates.put("specialInstructions", specialInstructions);
            
            String cartItemId = existingCartItem.get("_id").toString();
            boolean updated = DatabaseManager.update(COLLECTION, cartItemId, updates);
            
            if (updated) {
                System.out.println("🛒 Updated cart item quantity for customer: " + customerId);
                return true;
            }
        } else {
            // Add new item to cart
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("customerId", customerId);
            cartItem.put("itemId", itemId);
            cartItem.put("quantity", quantity);
            cartItem.put("specialInstructions", specialInstructions != null ? specialInstructions : "");
            
            String id = DatabaseManager.insert(COLLECTION, cartItem);
            
            if (id != null) {
                System.out.println("➕ Added item to cart for customer: " + customerId + " (Item: " + itemId + ", Qty: " + quantity + ")");
                return true;
            }
        }
        
        return false;
    }

    // Get cart items for customer
    public List<CartItem> getCartItems(String customerId) {
        List<Map<String, Object>> cartDocuments = DatabaseManager.findByField(COLLECTION, "customerId", customerId);
        List<CartItem> cartItems = new ArrayList<>();
        
        for (Map<String, Object> doc : cartDocuments) {
            int itemId = ((Number) doc.get("itemId")).intValue();
            Optional<MenuItem> menuItem = menuService.getMenuItemById(itemId);
            
            if (menuItem.isPresent()) {
                int quantity = ((Number) doc.get("quantity")).intValue();
                String instructions = (String) doc.getOrDefault("specialInstructions", "");
                
                CartItem cartItem = new CartItem(menuItem.get(), quantity, instructions);
                cartItems.add(cartItem);
            }
        }
        
        return cartItems;
    }

    // Update cart item quantity
    public boolean updateCartItemQuantity(String customerId, int itemId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeItemFromCart(customerId, itemId);
        }

        List<Map<String, Object>> cartItems = DatabaseManager.findByField(COLLECTION, "customerId", customerId);
        
        for (Map<String, Object> item : cartItems) {
            if (Objects.equals(item.get("itemId"), itemId)) {
                String cartItemId = item.get("_id").toString();
                Map<String, Object> updates = new HashMap<>();
                updates.put("quantity", newQuantity);
                
                boolean updated = DatabaseManager.update(COLLECTION, cartItemId, updates);
                if (updated) {
                    System.out.println("🔄 Updated cart item quantity for customer: " + customerId);
                    return true;
                }
                break;
            }
        }
        
        return false;
    }

    // Remove item from cart
    public boolean removeItemFromCart(String customerId, int itemId) {
        List<Map<String, Object>> cartItems = DatabaseManager.findByField(COLLECTION, "customerId", customerId);
        
        for (Map<String, Object> item : cartItems) {
            if (Objects.equals(item.get("itemId"), itemId)) {
                String cartItemId = item.get("_id").toString();
                boolean deleted = DatabaseManager.delete(COLLECTION, cartItemId);
                
                if (deleted) {
                    System.out.println("➖ Removed item from cart for customer: " + customerId);
                    return true;
                }
                break;
            }
        }
        
        return false;
    }

    // Clear entire cart
    public void clearCart(String customerId) {
        List<Map<String, Object>> cartItems = DatabaseManager.findByField(COLLECTION, "customerId", customerId);

        for (Map<String, Object> item : cartItems) {
            Object idObj = item.get("id");
            if (idObj != null) {
                String cartItemId = idObj.toString();
                DatabaseManager.delete(COLLECTION, cartItemId);
                System.out.println("🗑️ Removed cart item ID: " + cartItemId);
            }
        }

        System.out.println("🧹 Cleared cart for customer: " + customerId + " (" + cartItems.size() + " items removed)");
    }

    // Get cart item count
    public int getCartItemCount(String customerId) {
        List<CartItem> items = getCartItems(customerId);
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    // Get cart subtotal
    public double getCartSubtotal(String customerId) {
        List<CartItem> items = getCartItems(customerId);
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    // Check if cart is empty
    public boolean isCartEmpty(String customerId) {
        return getCartItemCount(customerId) == 0;
    }

    // Get cart summary
    public String getCartSummary(String customerId) {
        int itemCount = getCartItemCount(customerId);
        double subtotal = getCartSubtotal(customerId);
        return String.format("%d items, $%.2f", itemCount, subtotal);
    }

    // Display cart
    public void displayCart(String customerId) {
        List<CartItem> items = getCartItems(customerId);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                    SHOPPING CART");
        System.out.println("Customer: " + customerId);
        System.out.println("=".repeat(60));
        
        if (items.isEmpty()) {
            System.out.println("🛒 Your cart is empty.");
            System.out.println("=".repeat(60));
            return;
        }
        
        double subtotal = 0;
        int totalItems = 0;
        
        for (CartItem item : items) {
            MenuItem menuItem = item.getMenuItem();
            System.out.printf("🍽️  %s%n", menuItem.getName());
            System.out.printf("    Quantity: %d × $%.2f = $%.2f%n", 
                            item.getQuantity(), menuItem.getPrice(), item.getTotalPrice());
            
            if (!item.getSpecialInstructions().isEmpty()) {
                System.out.printf("    📝 Instructions: %s%n", item.getSpecialInstructions());
            }
            
            subtotal += item.getTotalPrice();
            totalItems += item.getQuantity();
            System.out.println();
        }
        
        System.out.println("-".repeat(60));
        System.out.printf("📊 Total Items: %d%n", totalItems);
        System.out.printf("💰 Subtotal: $%.2f%n", subtotal);
        System.out.println("=".repeat(60));
    }

    // Quick add popular item by category
    public boolean quickAddPopularItem(String customerId, String category) {
        List<MenuItem> categoryItems = menuService.getMenuItemsByCategory(category);
        
        if (!categoryItems.isEmpty()) {
            // Add the first available item from the category
            MenuItem item = categoryItems.get(0);
            return addItemToCart(customerId, item.getId(), 1, "Quick add");
        }
        
        return false;
    }

    // Display cart statistics for all customers
    public void displayCartStatistics() {
        List<Map<String, Object>> allCartItems = DatabaseManager.findAll(COLLECTION);
        
        if (allCartItems.isEmpty()) {
            System.out.println("📊 No cart data available.");
            return;
        }
        
        // Group by customer
        Map<String, List<Map<String, Object>>> cartsByCustomer = allCartItems.stream()
                .collect(Collectors.groupingBy(item -> (String) item.get("customerId")));
        
        System.out.println("📊 CART STATISTICS:");
        System.out.println("   Active Customers: " + cartsByCustomer.size());
        System.out.println("   Total Cart Items: " + allCartItems.size());
        
        for (Map.Entry<String, List<Map<String, Object>>> entry : cartsByCustomer.entrySet()) {
            String customerId = entry.getKey();
            int itemCount = getCartItemCount(customerId);
            double subtotal = getCartSubtotal(customerId);
            
            System.out.printf("   %s: %d items ($%.2f)%n", customerId, itemCount, subtotal);
        }
    }

    // Display all carts (admin function)
    public void displayAllCarts() {
        List<Map<String, Object>> allCartItems = DatabaseManager.findAll(COLLECTION);
        
        if (allCartItems.isEmpty()) {
            System.out.println("🛒 No active carts.");
            return;
        }
        
        // Group by customer
        Map<String, List<Map<String, Object>>> cartsByCustomer = allCartItems.stream()
                .collect(Collectors.groupingBy(item -> (String) item.get("customerId")));
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                           ALL CUSTOMER CARTS");
        System.out.println("=".repeat(80));
        
        for (String customerId : cartsByCustomer.keySet()) {
            displayCart(customerId);
        }
    }

    // Get real-time cart data for API
    public Map<String, Object> getCartData(String customerId) {
        List<CartItem> items = getCartItems(customerId);
        Map<String, Object> cartData = new HashMap<>();
        
        cartData.put("customerId", customerId);
        cartData.put("items", items);
        cartData.put("itemCount", getCartItemCount(customerId));
        cartData.put("subtotal", getCartSubtotal(customerId));
        cartData.put("isEmpty", isCartEmpty(customerId));
        cartData.put("lastUpdated", System.currentTimeMillis());
        
        return cartData;
    }
}
