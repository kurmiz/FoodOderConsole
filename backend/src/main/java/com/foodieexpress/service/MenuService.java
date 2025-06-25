package com.foodieexpress.service;

import com.foodieexpress.model.MenuItem;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing menu items
 */
public class MenuService {
    private final List<MenuItem> menuItems;
    private int nextId;

    public MenuService() {
        this.menuItems = new ArrayList<>();
        this.nextId = 1;
        initializeDefaultMenu();
    }

    // Initialize with default menu items
    private void initializeDefaultMenu() {
        // Pizza items
        addMenuItem("Margherita Pizza", "Fresh San Marzano tomatoes, buffalo mozzarella, fresh basil, extra virgin olive oil", 16.99, "pizza", "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3");
        addMenuItem("Pepperoni Supreme", "Premium pepperoni, mozzarella cheese, rich tomato sauce, oregano", 19.99, "pizza", "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b");
        addMenuItem("Truffle Mushroom Pizza", "Wild mushrooms, truffle oil, mozzarella, arugula, parmesan shavings", 24.99, "pizza", "https://images.unsplash.com/photo-1513104890138-7c749659a591");

        // Burger items
        addMenuItem("Gourmet Beef Burger", "Wagyu beef patty, aged cheddar, caramelized onions, truffle aioli, brioche bun", 18.99, "burger", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd");
        addMenuItem("BBQ Bacon Burger", "Beef patty, crispy bacon, BBQ sauce, onion rings, cheddar cheese", 16.99, "burger", "https://images.unsplash.com/photo-1571091718767-18b5b1457add");
        addMenuItem("Chicken Avocado Burger", "Grilled chicken breast, fresh avocado, lettuce, tomato, herb mayo", 15.99, "burger", "https://images.unsplash.com/photo-1606755962773-d324e9a13086");

        // Pasta items
        addMenuItem("Lobster Ravioli", "Fresh lobster ravioli in creamy saffron sauce with cherry tomatoes", 28.99, "pasta", "https://images.unsplash.com/photo-1551183053-bf91a1d81141");
        addMenuItem("Truffle Carbonara", "Fresh pasta with eggs, pecorino romano, pancetta, black truffle shavings", 22.99, "pasta", "https://images.unsplash.com/photo-1473093295043-cdd812d0e601");
        addMenuItem("Seafood Linguine", "Fresh linguine with shrimp, scallops, mussels in white wine garlic sauce", 26.99, "pasta", "https://images.unsplash.com/photo-1551892374-ecf8754cf8b0");

        // Dessert items
        addMenuItem("Chocolate Lava Cake", "Warm chocolate cake with molten center, vanilla ice cream, berry compote", 9.99, "dessert", "https://images.unsplash.com/photo-1578985545062-69928b1d9587");
        addMenuItem("Classic Tiramisu", "Traditional Italian dessert with mascarpone, coffee, cocoa, ladyfingers", 8.99, "dessert", "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9");
        addMenuItem("Crème Brûlée", "Vanilla custard with caramelized sugar crust, fresh berries", 10.99, "dessert", "https://images.unsplash.com/photo-1470324161839-ce2bb6fa6bc3");
    }

    // Add a new menu item
    public MenuItem addMenuItem(String name, String description, double price, String category, String imageUrl) {
        MenuItem item = new MenuItem(nextId++, name, description, price, category, imageUrl);
        menuItems.add(item);
        return item;
    }

    // Get all menu items
    public List<MenuItem> getAllMenuItems() {
        return new ArrayList<>(menuItems);
    }

    // Get available menu items only
    public List<MenuItem> getAvailableMenuItems() {
        return menuItems.stream()
                .filter(MenuItem::isAvailable)
                .collect(Collectors.toList());
    }

    // Get menu items by category
    public List<MenuItem> getMenuItemsByCategory(String category) {
        return menuItems.stream()
                .filter(item -> item.isAvailable() && 
                               item.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    // Get menu item by ID
    public Optional<MenuItem> getMenuItemById(int id) {
        return menuItems.stream()
                .filter(item -> item.getId() == id)
                .findFirst();
    }

    // Search menu items by name
    public List<MenuItem> searchMenuItems(String searchTerm) {
        String lowerSearchTerm = searchTerm.toLowerCase();
        return menuItems.stream()
                .filter(item -> item.isAvailable() &&
                               (item.getName().toLowerCase().contains(lowerSearchTerm) ||
                                item.getDescription().toLowerCase().contains(lowerSearchTerm)))
                .collect(Collectors.toList());
    }

    // Update menu item
    public boolean updateMenuItem(int id, String name, String description, double price, String category) {
        Optional<MenuItem> item = getMenuItemById(id);
        if (item.isPresent()) {
            MenuItem menuItem = item.get();
            if (name != null) menuItem.setName(name);
            if (description != null) menuItem.setDescription(description);
            if (price > 0) menuItem.setPrice(price);
            if (category != null) menuItem.setCategory(category);
            return true;
        }
        return false;
    }

    // Toggle menu item availability
    public boolean toggleAvailability(int id) {
        Optional<MenuItem> item = getMenuItemById(id);
        if (item.isPresent()) {
            MenuItem menuItem = item.get();
            menuItem.setAvailable(!menuItem.isAvailable());
            return true;
        }
        return false;
    }

    // Remove menu item
    public boolean removeMenuItem(int id) {
        return menuItems.removeIf(item -> item.getId() == id);
    }

    // Get all categories
    public Set<String> getAllCategories() {
        return menuItems.stream()
                .map(MenuItem::getCategory)
                .collect(Collectors.toSet());
    }

    // Get menu statistics
    public Map<String, Object> getMenuStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalItems", menuItems.size());
        stats.put("availableItems", getAvailableMenuItems().size());
        stats.put("categories", getAllCategories().size());
        
        Map<String, Long> categoryCount = menuItems.stream()
                .collect(Collectors.groupingBy(MenuItem::getCategory, Collectors.counting()));
        stats.put("itemsByCategory", categoryCount);
        
        OptionalDouble avgPrice = menuItems.stream()
                .filter(MenuItem::isAvailable)
                .mapToDouble(MenuItem::getPrice)
                .average();
        stats.put("averagePrice", avgPrice.orElse(0.0));
        
        return stats;
    }

    // Display all menu items
    public void displayAllMenuItems() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                           FOODIE EXPRESS MENU");
        System.out.println("=".repeat(80));
        
        Map<String, List<MenuItem>> itemsByCategory = menuItems.stream()
                .filter(MenuItem::isAvailable)
                .collect(Collectors.groupingBy(MenuItem::getCategory));
        
        for (Map.Entry<String, List<MenuItem>> entry : itemsByCategory.entrySet()) {
            System.out.println("\n" + entry.getKey().toUpperCase() + ":");
            System.out.println("-".repeat(40));
            
            for (MenuItem item : entry.getValue()) {
                System.out.println(item.toDisplayString());
                System.out.println();
            }
        }
        
        System.out.println("=".repeat(80));
    }

    // Display menu statistics
    public void displayMenuStatistics() {
        Map<String, Object> stats = getMenuStatistics();
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("                MENU STATISTICS");
        System.out.println("=".repeat(50));
        System.out.println("Total Items: " + stats.get("totalItems"));
        System.out.println("Available Items: " + stats.get("availableItems"));
        System.out.println("Categories: " + stats.get("categories"));
        System.out.printf("Average Price: $%.2f%n", (Double) stats.get("averagePrice"));
        
        System.out.println("\nItems by Category:");
        @SuppressWarnings("unchecked")
        Map<String, Long> categoryCount = (Map<String, Long>) stats.get("itemsByCategory");
        categoryCount.forEach((category, count) -> 
            System.out.println("  " + category + ": " + count + " items"));
        
        System.out.println("=".repeat(50));
    }
}
