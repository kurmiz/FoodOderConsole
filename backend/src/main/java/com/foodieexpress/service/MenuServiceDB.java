package com.foodieexpress.service;

import com.foodieexpress.model.MenuItem;
import com.foodieexpress.database.DatabaseManager;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Database-backed service class for managing menu items with real-time persistence
 */
public class MenuServiceDB {
    private static final String COLLECTION = "menu_items";

    public MenuServiceDB() {
        // Initialize database
        DatabaseManager.initialize();
    }

    // Get all menu items from database
    public List<MenuItem> getAllMenuItems() {
        List<Map<String, Object>> documents = DatabaseManager.findAll(COLLECTION);
        return documents.stream()
                .map(this::documentToMenuItem)
                .collect(Collectors.toList());
    }

    // Get available menu items only
    public List<MenuItem> getAvailableMenuItems() {
        return getAllMenuItems().stream()
                .filter(MenuItem::isAvailable)
                .collect(Collectors.toList());
    }

    // Get menu item by ID
    public Optional<MenuItem> getMenuItemById(int id) {
        Map<String, Object> document = DatabaseManager.findById(COLLECTION, String.valueOf(id));
        if (document != null) {
            return Optional.of(documentToMenuItem(document));
        }
        return Optional.empty();
    }

    // Get menu items by category
    public List<MenuItem> getMenuItemsByCategory(String category) {
        List<Map<String, Object>> documents = DatabaseManager.findByField(COLLECTION, "category", category);
        return documents.stream()
                .map(this::documentToMenuItem)
                .filter(MenuItem::isAvailable)
                .collect(Collectors.toList());
    }

    // Search menu items
    public List<MenuItem> searchMenuItems(String searchTerm) {
        String lowerSearchTerm = searchTerm.toLowerCase();
        return getAvailableMenuItems().stream()
                .filter(item -> 
                    item.getName().toLowerCase().contains(lowerSearchTerm) ||
                    item.getDescription().toLowerCase().contains(lowerSearchTerm) ||
                    item.getCategory().toLowerCase().contains(lowerSearchTerm)
                )
                .collect(Collectors.toList());
    }

    // Add a new menu item
    public MenuItem addMenuItem(String name, String description, double price, String category, String imageUrl) {
        Map<String, Object> document = new HashMap<>();
        document.put("name", name);
        document.put("description", description);
        document.put("price", price);
        document.put("category", category);
        document.put("imageUrl", imageUrl != null ? imageUrl : "");
        document.put("available", true);

        String id = DatabaseManager.insert(COLLECTION, document);
        
        if (id != null) {
            MenuItem item = new MenuItem(Integer.parseInt(id), name, description, price, category, imageUrl);
            System.out.println("✅ New menu item added to database: " + name + " (ID: " + id + ")");
            System.out.println("📋 Total menu items: " + DatabaseManager.count(COLLECTION));
            return item;
        }
        
        return null;
    }

    // Delete a menu item
    public boolean deleteMenuItem(int itemId) {
        Optional<MenuItem> item = getMenuItemById(itemId);
        if (item.isPresent()) {
            boolean deleted = DatabaseManager.delete(COLLECTION, String.valueOf(itemId));
            if (deleted) {
                System.out.println("🗑️ Menu item deleted from database: " + item.get().getName() + " (ID: " + itemId + ")");
                System.out.println("📋 Total menu items: " + DatabaseManager.count(COLLECTION));
                return true;
            }
        }
        return false;
    }

    // Update a menu item
    public boolean updateMenuItem(int itemId, String name, String description, double price, String category, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        if (name != null) updates.put("name", name);
        if (description != null) updates.put("description", description);
        if (price > 0) updates.put("price", price);
        if (category != null) updates.put("category", category);
        if (imageUrl != null) updates.put("imageUrl", imageUrl);

        boolean updated = DatabaseManager.update(COLLECTION, String.valueOf(itemId), updates);
        if (updated) {
            System.out.println("✏️ Menu item updated in database: " + name + " (ID: " + itemId + ")");
            return true;
        }
        return false;
    }

    // Toggle item availability
    public boolean toggleAvailability(int itemId) {
        Optional<MenuItem> item = getMenuItemById(itemId);
        if (item.isPresent()) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("available", !item.get().isAvailable());
            
            boolean updated = DatabaseManager.update(COLLECTION, String.valueOf(itemId), updates);
            if (updated) {
                System.out.println("🔄 Menu item availability toggled: " + item.get().getName() + " (ID: " + itemId + ")");
                return true;
            }
        }
        return false;
    }

    // Get all categories
    public Set<String> getAllCategories() {
        return getAllMenuItems().stream()
                .map(MenuItem::getCategory)
                .collect(Collectors.toSet());
    }

    // Display all menu items
    public void displayAllMenuItems() {
        List<MenuItem> items = getAllMenuItems();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                           COMPLETE MENU");
        System.out.println("=".repeat(80));
        
        if (items.isEmpty()) {
            System.out.println("No menu items available.");
            return;
        }
        
        Map<String, List<MenuItem>> itemsByCategory = items.stream()
                .collect(Collectors.groupingBy(MenuItem::getCategory));
        
        for (Map.Entry<String, List<MenuItem>> entry : itemsByCategory.entrySet()) {
            String category = entry.getKey().toUpperCase();
            List<MenuItem> categoryItems = entry.getValue();
            
            System.out.println("\n📋 " + category + " (" + categoryItems.size() + " items)");
            System.out.println("-".repeat(50));
            
            for (MenuItem item : categoryItems) {
                System.out.println(item.toDisplayString());
                System.out.println();
            }
        }
        
        System.out.println("=".repeat(80));
        System.out.println("Total Items: " + items.size() + " | Available: " + 
                          items.stream().mapToInt(item -> item.isAvailable() ? 1 : 0).sum());
        System.out.println("=".repeat(80));
    }

    // Display menu statistics
    public void displayMenuStatistics() {
        List<MenuItem> allItems = getAllMenuItems();
        List<MenuItem> availableItems = getAvailableMenuItems();
        Set<String> categories = getAllCategories();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                    MENU STATISTICS");
        System.out.println("=".repeat(60));
        System.out.println("📊 Total Items: " + allItems.size());
        System.out.println("✅ Available Items: " + availableItems.size());
        System.out.println("❌ Unavailable Items: " + (allItems.size() - availableItems.size()));
        System.out.println("📁 Categories: " + categories.size());
        
        if (!categories.isEmpty()) {
            System.out.println("\n📋 Items by Category:");
            for (String category : categories) {
                long count = allItems.stream()
                        .filter(item -> item.getCategory().equals(category))
                        .count();
                System.out.println("  " + category + ": " + count + " items");
            }
        }
        
        if (!allItems.isEmpty()) {
            double avgPrice = allItems.stream()
                    .mapToDouble(MenuItem::getPrice)
                    .average()
                    .orElse(0.0);
            
            double minPrice = allItems.stream()
                    .mapToDouble(MenuItem::getPrice)
                    .min()
                    .orElse(0.0);
            
            double maxPrice = allItems.stream()
                    .mapToDouble(MenuItem::getPrice)
                    .max()
                    .orElse(0.0);
            
            System.out.println("\n💰 Price Statistics:");
            System.out.printf("  Average Price: $%.2f%n", avgPrice);
            System.out.printf("  Price Range: $%.2f - $%.2f%n", minPrice, maxPrice);
        }
        
        System.out.println("=".repeat(60));
        
        // Display database statistics
        DatabaseManager.displayDatabaseStats();
    }

    // Convert database document to MenuItem
    private MenuItem documentToMenuItem(Map<String, Object> document) {
        int id = ((Number) document.get("id")).intValue();
        String name = (String) document.get("name");
        String description = (String) document.get("description");
        double price = ((Number) document.get("price")).doubleValue();
        String category = (String) document.get("category");
        String imageUrl = (String) document.getOrDefault("imageUrl", "");
        boolean available = (Boolean) document.getOrDefault("available", true);
        
        MenuItem item = new MenuItem(id, name, description, price, category, imageUrl);
        item.setAvailable(available);
        return item;
    }

    // Get database statistics
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalItems", DatabaseManager.count(COLLECTION));
        stats.put("availableItems", getAvailableMenuItems().size());
        stats.put("categories", getAllCategories().size());
        
        List<MenuItem> items = getAllMenuItems();
        if (!items.isEmpty()) {
            double avgPrice = items.stream()
                    .mapToDouble(MenuItem::getPrice)
                    .average()
                    .orElse(0.0);
            stats.put("averagePrice", avgPrice);
        } else {
            stats.put("averagePrice", 0.0);
        }
        
        return stats;
    }

    // Real-time data refresh
    public void refreshFromDatabase() {
        // This method can be called to ensure we have the latest data
        // Since we're reading from database each time, this is automatic
        System.out.println("🔄 Menu data refreshed from database");
    }
}
