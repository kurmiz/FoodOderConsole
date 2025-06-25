package com.foodieexpress.service;

import com.foodieexpress.model.Cart;
import com.foodieexpress.model.CartItem;
import com.foodieexpress.model.MenuItem;
import java.util.*;

/**
 * Service class for managing shopping carts
 */
public class CartService {
    private final Map<String, Cart> customerCarts;
    private final MenuService menuService;

    public CartService(MenuService menuService) {
        this.customerCarts = new HashMap<>();
        this.menuService = menuService;
    }

    // Get or create cart for customer
    public Cart getCart(String customerId) {
        return customerCarts.computeIfAbsent(customerId, Cart::new);
    }

    // Add item to cart
    public boolean addItemToCart(String customerId, int menuItemId, int quantity) {
        return addItemToCart(customerId, menuItemId, quantity, "");
    }

    // Add item to cart with special instructions
    public boolean addItemToCart(String customerId, int menuItemId, int quantity, String specialInstructions) {
        Optional<MenuItem> menuItem = menuService.getMenuItemById(menuItemId);
        
        if (menuItem.isPresent() && menuItem.get().isAvailable() && quantity > 0) {
            Cart cart = getCart(customerId);
            cart.addItem(menuItem.get(), quantity, specialInstructions);
            return true;
        }
        return false;
    }

    // Remove item from cart
    public boolean removeItemFromCart(String customerId, int menuItemId) {
        Cart cart = customerCarts.get(customerId);
        if (cart != null) {
            return cart.removeItem(menuItemId);
        }
        return false;
    }

    // Update item quantity in cart
    public boolean updateCartItemQuantity(String customerId, int menuItemId, int newQuantity) {
        Cart cart = customerCarts.get(customerId);
        if (cart != null) {
            return cart.updateItemQuantity(menuItemId, newQuantity);
        }
        return false;
    }

    // Clear cart
    public void clearCart(String customerId) {
        Cart cart = customerCarts.get(customerId);
        if (cart != null) {
            cart.clearCart();
        }
    }

    // Get cart items
    public List<CartItem> getCartItems(String customerId) {
        Cart cart = customerCarts.get(customerId);
        return cart != null ? cart.getItems() : new ArrayList<>();
    }

    // Get cart subtotal
    public double getCartSubtotal(String customerId) {
        Cart cart = customerCarts.get(customerId);
        return cart != null ? cart.getSubtotal() : 0.0;
    }

    // Get cart item count
    public int getCartItemCount(String customerId) {
        Cart cart = customerCarts.get(customerId);
        return cart != null ? cart.getTotalItems() : 0;
    }

    // Check if cart is empty
    public boolean isCartEmpty(String customerId) {
        Cart cart = customerCarts.get(customerId);
        return cart == null || cart.isEmpty();
    }

    // Get all customer carts (for admin purposes)
    public Map<String, Cart> getAllCarts() {
        return new HashMap<>(customerCarts);
    }

    // Remove empty carts
    public void removeEmptyCarts() {
        customerCarts.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    // Get cart statistics
    public Map<String, Object> getCartStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        int totalCarts = customerCarts.size();
        int nonEmptyCarts = (int) customerCarts.values().stream()
                .filter(cart -> !cart.isEmpty())
                .count();
        
        double totalValue = customerCarts.values().stream()
                .mapToDouble(Cart::getSubtotal)
                .sum();
        
        int totalItems = customerCarts.values().stream()
                .mapToInt(Cart::getTotalItems)
                .sum();
        
        OptionalDouble avgCartValue = customerCarts.values().stream()
                .filter(cart -> !cart.isEmpty())
                .mapToDouble(Cart::getSubtotal)
                .average();
        
        stats.put("totalCarts", totalCarts);
        stats.put("nonEmptyCarts", nonEmptyCarts);
        stats.put("totalValue", totalValue);
        stats.put("totalItems", totalItems);
        stats.put("averageCartValue", avgCartValue.orElse(0.0));
        
        return stats;
    }

    // Display cart for customer
    public void displayCart(String customerId) {
        Cart cart = customerCarts.get(customerId);
        
        if (cart == null || cart.isEmpty()) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Cart is empty for customer: " + customerId);
            System.out.println("=".repeat(50));
            return;
        }
        
        System.out.println("\n" + cart.toDisplayString());
    }

    // Display all carts (admin view)
    public void displayAllCarts() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                           ALL CUSTOMER CARTS");
        System.out.println("=".repeat(80));
        
        if (customerCarts.isEmpty()) {
            System.out.println("No carts found.");
            System.out.println("=".repeat(80));
            return;
        }
        
        for (Map.Entry<String, Cart> entry : customerCarts.entrySet()) {
            Cart cart = entry.getValue();
            if (!cart.isEmpty()) {
                System.out.println(cart.toDisplayString());
                System.out.println();
            }
        }
        
        System.out.println("=".repeat(80));
    }

    // Display cart statistics
    public void displayCartStatistics() {
        Map<String, Object> stats = getCartStatistics();
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("                CART STATISTICS");
        System.out.println("=".repeat(50));
        System.out.println("Total Carts: " + stats.get("totalCarts"));
        System.out.println("Non-Empty Carts: " + stats.get("nonEmptyCarts"));
        System.out.println("Total Items in All Carts: " + stats.get("totalItems"));
        System.out.printf("Total Value of All Carts: $%.2f%n", (Double) stats.get("totalValue"));
        System.out.printf("Average Cart Value: $%.2f%n", (Double) stats.get("averageCartValue"));
        System.out.println("=".repeat(50));
    }

    // Quick add popular items to cart
    public boolean quickAddPopularItem(String customerId, String category) {
        List<MenuItem> categoryItems = menuService.getMenuItemsByCategory(category);
        
        if (!categoryItems.isEmpty()) {
            // Add the first item from the category (assuming it's popular)
            MenuItem popularItem = categoryItems.get(0);
            return addItemToCart(customerId, popularItem.getId(), 1);
        }
        return false;
    }

    // Get cart summary for customer
    public String getCartSummary(String customerId) {
        Cart cart = customerCarts.get(customerId);
        if (cart != null) {
            return cart.getCartSummary();
        }
        return "Cart not found";
    }

    // Validate cart before checkout
    public boolean validateCart(String customerId) {
        Cart cart = customerCarts.get(customerId);
        
        if (cart == null || cart.isEmpty()) {
            return false;
        }
        
        // Check if all items in cart are still available
        for (CartItem item : cart.getItems()) {
            Optional<MenuItem> menuItem = menuService.getMenuItemById(item.getMenuItem().getId());
            if (menuItem.isEmpty() || !menuItem.get().isAvailable()) {
                return false;
            }
        }
        
        return true;
    }
}
