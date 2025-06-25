package com.foodieexpress.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

/**
 * MongoDB connection and database management
 */
public class MongoDBConnection {
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "foodieexpress";
    
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    
    public static void connect() {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
            System.out.println("✅ Connected to MongoDB: " + DATABASE_NAME);
            
            // Create collections if they don't exist
            createCollections();
            
        } catch (Exception e) {
            System.err.println("❌ Failed to connect to MongoDB: " + e.getMessage());
            System.out.println("💡 Make sure MongoDB is running on localhost:27017");
            System.out.println("💡 Using in-memory storage as fallback");
        }
    }
    
    private static void createCollections() {
        try {
            // Create collections
            createCollectionIfNotExists("menu_items");
            createCollectionIfNotExists("cart_items");
            createCollectionIfNotExists("orders");
            createCollectionIfNotExists("customers");
            
            System.out.println("✅ MongoDB collections initialized");
        } catch (Exception e) {
            System.err.println("❌ Error creating collections: " + e.getMessage());
        }
    }
    
    private static void createCollectionIfNotExists(String collectionName) {
        try {
            // Check if collection exists
            boolean exists = false;
            for (String name : database.listCollectionNames()) {
                if (name.equals(collectionName)) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                database.createCollection(collectionName);
                System.out.println("📁 Created collection: " + collectionName);
            }
        } catch (Exception e) {
            System.err.println("❌ Error checking/creating collection " + collectionName + ": " + e.getMessage());
        }
    }
    
    public static MongoDatabase getDatabase() {
        return database;
    }
    
    public static MongoCollection<Document> getCollection(String collectionName) {
        if (database != null) {
            return database.getCollection(collectionName);
        }
        return null;
    }
    
    public static boolean isConnected() {
        return database != null;
    }
    
    public static void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("🔌 Disconnected from MongoDB");
        }
    }
    
    // Initialize sample data if collections are empty
    public static void initializeSampleData() {
        if (!isConnected()) return;
        
        try {
            MongoCollection<Document> menuCollection = getCollection("menu_items");
            
            // Check if menu items exist
            if (menuCollection.countDocuments() == 0) {
                System.out.println("📋 Initializing sample menu data...");
                
                // Add sample menu items
                Document[] sampleItems = {
                    new Document("id", 1)
                        .append("name", "Margherita Pizza")
                        .append("description", "Fresh San Marzano tomatoes, buffalo mozzarella, fresh basil, extra virgin olive oil")
                        .append("price", 16.99)
                        .append("category", "pizza")
                        .append("imageUrl", "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3")
                        .append("available", true),
                    
                    new Document("id", 2)
                        .append("name", "Pepperoni Supreme")
                        .append("description", "Premium pepperoni, mozzarella cheese, rich tomato sauce, oregano")
                        .append("price", 19.99)
                        .append("category", "pizza")
                        .append("imageUrl", "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b")
                        .append("available", true),
                    
                    new Document("id", 3)
                        .append("name", "Truffle Mushroom Pizza")
                        .append("description", "Wild mushrooms, truffle oil, mozzarella, arugula, parmesan shavings")
                        .append("price", 24.99)
                        .append("category", "pizza")
                        .append("imageUrl", "https://images.unsplash.com/photo-1513104890138-7c749659a591")
                        .append("available", true),
                    
                    new Document("id", 4)
                        .append("name", "Gourmet Beef Burger")
                        .append("description", "Wagyu beef patty, aged cheddar, caramelized onions, truffle aioli, brioche bun")
                        .append("price", 18.99)
                        .append("category", "burger")
                        .append("imageUrl", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd")
                        .append("available", true),
                    
                    new Document("id", 5)
                        .append("name", "BBQ Bacon Burger")
                        .append("description", "Beef patty, crispy bacon, BBQ sauce, onion rings, cheddar cheese")
                        .append("price", 16.99)
                        .append("category", "burger")
                        .append("imageUrl", "https://images.unsplash.com/photo-1571091718767-18b5b1457add")
                        .append("available", true),
                    
                    new Document("id", 6)
                        .append("name", "Chicken Avocado Burger")
                        .append("description", "Grilled chicken breast, fresh avocado, lettuce, tomato, herb mayo")
                        .append("price", 15.99)
                        .append("category", "burger")
                        .append("imageUrl", "https://images.unsplash.com/photo-1606755962773-d324e9a13086")
                        .append("available", true),
                    
                    new Document("id", 7)
                        .append("name", "Lobster Ravioli")
                        .append("description", "Fresh lobster ravioli in creamy saffron sauce with cherry tomatoes")
                        .append("price", 28.99)
                        .append("category", "pasta")
                        .append("imageUrl", "https://images.unsplash.com/photo-1551183053-bf91a1d81141")
                        .append("available", true),
                    
                    new Document("id", 8)
                        .append("name", "Truffle Carbonara")
                        .append("description", "Fresh pasta with eggs, pecorino romano, pancetta, black truffle shavings")
                        .append("price", 22.99)
                        .append("category", "pasta")
                        .append("imageUrl", "https://images.unsplash.com/photo-1473093295043-cdd812d0e601")
                        .append("available", true),
                    
                    new Document("id", 9)
                        .append("name", "Seafood Linguine")
                        .append("description", "Fresh linguine with shrimp, scallops, mussels in white wine garlic sauce")
                        .append("price", 26.99)
                        .append("category", "pasta")
                        .append("imageUrl", "https://images.unsplash.com/photo-1551892374-ecf8754cf8b0")
                        .append("available", true),
                    
                    new Document("id", 10)
                        .append("name", "Chocolate Lava Cake")
                        .append("description", "Warm chocolate cake with molten center, vanilla ice cream, berry compote")
                        .append("price", 9.99)
                        .append("category", "dessert")
                        .append("imageUrl", "https://images.unsplash.com/photo-1578985545062-69928b1d9587")
                        .append("available", true),
                    
                    new Document("id", 11)
                        .append("name", "Classic Tiramisu")
                        .append("description", "Traditional Italian dessert with mascarpone, coffee, cocoa, ladyfingers")
                        .append("price", 8.99)
                        .append("category", "dessert")
                        .append("imageUrl", "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9")
                        .append("available", true),
                    
                    new Document("id", 12)
                        .append("name", "Crème Brûlée")
                        .append("description", "Vanilla custard with caramelized sugar crust, fresh berries")
                        .append("price", 10.99)
                        .append("category", "dessert")
                        .append("imageUrl", "https://images.unsplash.com/photo-1470324161839-ce2bb6fa6bc3")
                        .append("available", true)
                };
                
                for (Document item : sampleItems) {
                    menuCollection.insertOne(item);
                }
                
                System.out.println("✅ Sample menu data initialized: " + sampleItems.length + " items");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing sample data: " + e.getMessage());
        }
    }
}
