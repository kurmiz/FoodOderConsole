package com.foodieexpress.database;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real-time database manager with file-based persistence
 * Simulates MongoDB operations with immediate persistence
 */
public class DatabaseManager {
    private static final String DATA_DIR = "data";
    private static final Map<String, Map<String, Map<String, Object>>> database = new ConcurrentHashMap<>();
    private static boolean initialized = false;
    private static int nextId = 1;
    
    public static synchronized void initialize() {
        if (initialized) return;
        
        try {
            // Create data directory
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
                System.out.println("📁 Created database directory: " + DATA_DIR);
            }
            
            // Initialize collections
            database.put("menu_items", new ConcurrentHashMap<>());
            database.put("cart_items", new ConcurrentHashMap<>());
            database.put("orders", new ConcurrentHashMap<>());
            database.put("customers", new ConcurrentHashMap<>());
            
            // Load existing data
            loadAllCollections();
            
            // Initialize with sample data if empty
            if (getCollection("menu_items").isEmpty()) {
                initializeSampleMenuData();
            }
            
            // Set next ID based on existing data
            updateNextId();
            
            initialized = true;
            System.out.println("✅ Real-time database initialized successfully");
            displayDatabaseStats();
            
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // CRUD Operations
    public static synchronized String insert(String collection, Map<String, Object> document) {
        try {
            Map<String, Map<String, Object>> coll = getCollection(collection);
            
            // Generate ID if not present
            String id;
            if (document.containsKey("id")) {
                id = document.get("id").toString();
            } else {
                id = String.valueOf(nextId++);
                document.put("id", Integer.parseInt(id));
            }
            
            // Add timestamps
            long now = System.currentTimeMillis();
            document.put("createdAt", now);
            document.put("updatedAt", now);
            
            // Store document
            coll.put(id, new HashMap<>(document));
            
            // Persist immediately
            saveCollection(collection);
            
            System.out.println("💾 [" + collection + "] Inserted document with ID: " + id);
            return id;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to insert document: " + e.getMessage());
            return null;
        }
    }
    
    public static List<Map<String, Object>> findAll(String collection) {
        Map<String, Map<String, Object>> coll = getCollection(collection);
        return new ArrayList<>(coll.values());
    }
    
    public static Map<String, Object> findById(String collection, String id) {
        Map<String, Map<String, Object>> coll = getCollection(collection);
        return coll.get(id);
    }
    
    public static List<Map<String, Object>> findByField(String collection, String field, Object value) {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Map<String, Object>> coll = getCollection(collection);
        
        for (Map<String, Object> doc : coll.values()) {
            if (Objects.equals(doc.get(field), value)) {
                results.add(doc);
            }
        }
        return results;
    }
    
    public static synchronized boolean update(String collection, String id, Map<String, Object> updates) {
        try {
            Map<String, Map<String, Object>> coll = getCollection(collection);
            Map<String, Object> document = coll.get(id);
            
            if (document != null) {
                // Update fields
                document.putAll(updates);
                document.put("updatedAt", System.currentTimeMillis());
                
                // Persist immediately
                saveCollection(collection);
                
                System.out.println("✏️ [" + collection + "] Updated document ID: " + id);
                return true;
            }
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to update document: " + e.getMessage());
            return false;
        }
    }
    
    public static synchronized boolean delete(String collection, String id) {
        try {
            Map<String, Map<String, Object>> coll = getCollection(collection);
            Map<String, Object> removed = coll.remove(id);
            
            if (removed != null) {
                // Persist immediately
                saveCollection(collection);
                
                System.out.println("🗑️ [" + collection + "] Deleted document ID: " + id);
                return true;
            }
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to delete document: " + e.getMessage());
            return false;
        }
    }
    
    public static long count(String collection) {
        return getCollection(collection).size();
    }
    
    // Collection management
    private static Map<String, Map<String, Object>> getCollection(String collection) {
        return database.computeIfAbsent(collection, k -> new ConcurrentHashMap<>());
    }
    
    // Persistence operations
    private static void saveCollection(String collection) {
        try {
            File file = new File(DATA_DIR, collection + ".json");
            Map<String, Map<String, Object>> coll = getCollection(collection);
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println(collectionToJson(coll));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to save collection " + collection + ": " + e.getMessage());
        }
    }
    
    private static void loadAllCollections() {
        try {
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists()) return;
            
            File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files == null) return;
            
            for (File file : files) {
                String collection = file.getName().replace(".json", "");
                loadCollection(collection);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to load collections: " + e.getMessage());
        }
    }
    
    private static void loadCollection(String collection) {
        try {
            File file = new File(DATA_DIR, collection + ".json");
            if (!file.exists()) return;
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            }
            
            if (content.length() > 0) {
                Map<String, Map<String, Object>> coll = jsonToCollection(content.toString());
                database.put(collection, coll);
                System.out.println("📂 Loaded collection: " + collection + " (" + coll.size() + " documents)");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to load collection " + collection + ": " + e.getMessage());
        }
    }
    
    private static void updateNextId() {
        int maxId = 0;
        for (Map<String, Map<String, Object>> collection : database.values()) {
            for (Map<String, Object> doc : collection.values()) {
                Object idObj = doc.get("id");
                if (idObj instanceof Number) {
                    maxId = Math.max(maxId, ((Number) idObj).intValue());
                }
            }
        }
        nextId = maxId + 1;
        System.out.println("🔢 Next ID set to: " + nextId);
    }
    
    // JSON serialization
    private static String collectionToJson(Map<String, Map<String, Object>> collection) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Map<String, Object>> entry : collection.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            json.append(documentToJson(entry.getValue()));
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    
    private static String documentToJson(Map<String, Object> document) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(escapeJson(entry.getKey())).append("\":");
            json.append(valueToJson(entry.getValue()));
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    
    private static String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeJson(value.toString()) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map) return documentToJson((Map<String, Object>) value);
        return "\"" + escapeJson(value.toString()) + "\"";
    }
    
    private static String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    // JSON deserialization (simplified)
    private static Map<String, Map<String, Object>> jsonToCollection(String json) {
        Map<String, Map<String, Object>> collection = new ConcurrentHashMap<>();
        
        try {
            // Simple JSON parsing for our use case
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                
                // Parse each document
                String[] parts = splitJsonObjects(json);
                for (String part : parts) {
                    String[] keyValue = part.split(":", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replaceAll("\"", "");
                        String docJson = keyValue[1].trim();
                        
                        Map<String, Object> document = parseJsonDocument(docJson);
                        if (!document.isEmpty()) {
                            collection.put(key, document);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to parse collection JSON: " + e.getMessage());
        }
        
        return collection;
    }
    
    private static String[] splitJsonObjects(String json) {
        List<String> objects = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int braceLevel = 0;
        boolean inQuotes = false;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            
            if (!inQuotes) {
                if (c == '{') braceLevel++;
                else if (c == '}') braceLevel--;
                else if (c == ',' && braceLevel == 0) {
                    objects.add(current.toString().trim());
                    current = new StringBuilder();
                    continue;
                }
            }
            
            current.append(c);
        }
        
        if (current.length() > 0) {
            objects.add(current.toString().trim());
        }
        
        return objects.toArray(new String[0]);
    }
    
    private static Map<String, Object> parseJsonDocument(String json) {
        Map<String, Object> document = new HashMap<>();
        
        try {
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                
                String[] pairs = splitJsonPairs(json);
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replaceAll("\"", "");
                        String value = keyValue[1].trim();
                        
                        Object parsedValue = parseJsonValue(value);
                        document.put(key, parsedValue);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to parse document JSON: " + e.getMessage());
        }
        
        return document;
    }
    
    private static String[] splitJsonPairs(String json) {
        List<String> pairs = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int braceLevel = 0;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            
            if (!inQuotes) {
                if (c == '{') braceLevel++;
                else if (c == '}') braceLevel--;
                else if (c == ',' && braceLevel == 0) {
                    pairs.add(current.toString().trim());
                    current = new StringBuilder();
                    continue;
                }
            }
            
            current.append(c);
        }
        
        if (current.length() > 0) {
            pairs.add(current.toString().trim());
        }
        
        return pairs.toArray(new String[0]);
    }
    
    private static Object parseJsonValue(String value) {
        value = value.trim();
        
        if (value.equals("null")) return null;
        if (value.equals("true")) return true;
        if (value.equals("false")) return false;
        
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1)
                       .replace("\\\"", "\"")
                       .replace("\\n", "\n")
                       .replace("\\r", "\r")
                       .replace("\\t", "\t")
                       .replace("\\\\", "\\");
        }
        
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }
    
    private static void initializeSampleMenuData() {
        System.out.println("🌱 Initializing sample menu data...");
        
        String[][] sampleItems = {
            {"Margherita Pizza", "Fresh San Marzano tomatoes, buffalo mozzarella, fresh basil, extra virgin olive oil", "16.99", "pizza", "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3"},
            {"Pepperoni Supreme", "Premium pepperoni, mozzarella cheese, rich tomato sauce, oregano", "19.99", "pizza", "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b"},
            {"Truffle Mushroom Pizza", "Wild mushrooms, truffle oil, mozzarella, arugula, parmesan shavings", "24.99", "pizza", "https://images.unsplash.com/photo-1513104890138-7c749659a591"},
            {"Gourmet Beef Burger", "Wagyu beef patty, aged cheddar, caramelized onions, truffle aioli, brioche bun", "18.99", "burger", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd"},
            {"BBQ Bacon Burger", "Beef patty, crispy bacon, BBQ sauce, onion rings, cheddar cheese", "16.99", "burger", "https://images.unsplash.com/photo-1571091718767-18b5b1457add"},
            {"Chicken Avocado Burger", "Grilled chicken breast, fresh avocado, lettuce, tomato, herb mayo", "15.99", "burger", "https://images.unsplash.com/photo-1606755962773-d324e9a13086"},
            {"Lobster Ravioli", "Fresh lobster ravioli in creamy saffron sauce with cherry tomatoes", "28.99", "pasta", "https://images.unsplash.com/photo-1551183053-bf91a1d81141"},
            {"Truffle Carbonara", "Fresh pasta with eggs, pecorino romano, pancetta, black truffle shavings", "22.99", "pasta", "https://images.unsplash.com/photo-1473093295043-cdd812d0e601"},
            {"Seafood Linguine", "Fresh linguine with shrimp, scallops, mussels in white wine garlic sauce", "26.99", "pasta", "https://images.unsplash.com/photo-1551892374-ecf8754cf8b0"},
            {"Chocolate Lava Cake", "Warm chocolate cake with molten center, vanilla ice cream, berry compote", "9.99", "dessert", "https://images.unsplash.com/photo-1578985545062-69928b1d9587"},
            {"Classic Tiramisu", "Traditional Italian dessert with mascarpone, coffee, cocoa, ladyfingers", "8.99", "dessert", "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9"},
            {"Crème Brûlée", "Vanilla custard with caramelized sugar crust, fresh berries", "10.99", "dessert", "https://images.unsplash.com/photo-1470324161839-ce2bb6fa6bc3"}
        };
        
        for (int i = 0; i < sampleItems.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i + 1);
            item.put("name", sampleItems[i][0]);
            item.put("description", sampleItems[i][1]);
            item.put("price", Double.parseDouble(sampleItems[i][2]));
            item.put("category", sampleItems[i][3]);
            item.put("imageUrl", sampleItems[i][4]);
            item.put("available", true);
            
            insert("menu_items", item);
        }
        
        System.out.println("✅ Sample menu data initialized: " + sampleItems.length + " items");
    }
    
    public static void displayDatabaseStats() {
        System.out.println("\n📊 REAL-TIME DATABASE STATISTICS:");
        System.out.println("Collections: " + database.size());
        for (String collection : database.keySet()) {
            System.out.println("  📁 " + collection + ": " + count(collection) + " documents");
        }
        System.out.println("💾 Data persisted to: " + new File(DATA_DIR).getAbsolutePath());
    }
}
