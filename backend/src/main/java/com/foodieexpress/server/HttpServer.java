package com.foodieexpress.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.foodieexpress.service.MenuService;
import com.foodieexpress.service.CartService;
import com.foodieexpress.service.OrderService;
import com.foodieexpress.model.MenuItem;
import com.foodieexpress.model.CartItem;
import com.foodieexpress.model.Order;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Simple HTTP Server to serve the frontend and provide REST API endpoints
 */
public class HttpServer {
    private final com.sun.net.httpserver.HttpServer server;
    private final MenuService menuService;
    private final CartService cartService;
    private final OrderService orderService;
    private final int port;

    public HttpServer(int port, MenuService menuService, CartService cartService, OrderService orderService) throws IOException {
        this.port = port;
        this.menuService = menuService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
        setupRoutes();
    }

    private void setupRoutes() {
        // API endpoints (must be registered before static handler)
        server.createContext("/api/menu", new MenuHandler());
        server.createContext("/api/cart", new CartHandler());
        server.createContext("/api/orders", new OrderHandler());

        // Serve static files (frontend) - this should be last
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
    }

    public void start() {
        server.start();
        System.out.println("🚀 FoodieExpress Server started on http://localhost:" + port);
        System.out.println("📱 Frontend: http://localhost:" + port);
        System.out.println("🔗 API Base: http://localhost:" + port + "/api");
        System.out.println("=" .repeat(60));
    }

    public void stop() {
        server.stop(0);
        System.out.println("Server stopped.");
    }

    // Static file handler for serving frontend files
    class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Default to index.html
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            // Serve files from frontend directory
            String filePath = "../frontend" + path;
            File file = new File(filePath);
            
            if (file.exists() && file.isFile()) {
                String contentType = getContentType(path);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                exchange.sendResponseHeaders(200, fileBytes.length);
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(fileBytes);
                }
            } else {
                // 404 Not Found
                String response = "File not found: " + path;
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".ico")) return "image/x-icon";
            return "text/plain";
        }
    }

    // Menu API handler
    class MenuHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("🔗 API Request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath());

            setCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            try {
                switch (method) {
                    case "GET":
                        handleGetMenu(exchange);
                        break;
                    case "POST":
                        handleAddMenuItem(exchange);
                        break;
                    case "PUT":
                        handleUpdateMenuItem(exchange);
                        break;
                    case "DELETE":
                        handleDeleteMenuItem(exchange);
                        break;
                    default:
                        sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception e) {
                System.err.println("❌ Menu API Error: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
        
        private void handleGetMenu(HttpExchange exchange) throws IOException {
            List<MenuItem> items = menuService.getAvailableMenuItems();
            String json = menuItemsToJson(items);
            sendResponse(exchange, 200, json);

            // Also display in CLI
            System.out.println("📋 Menu requested via API - " + items.size() + " items served");
        }

        private void handleAddMenuItem(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseJson(body);

            try {
                String name = params.get("name");
                String description = params.get("description");
                double price = Double.parseDouble(params.get("price"));
                String category = params.get("category");
                String imageUrl = params.getOrDefault("imageUrl", "");

                MenuItem newItem = menuService.addMenuItem(name, description, price, category, imageUrl);

                String json = String.format(
                    "{\"success\":true,\"message\":\"Menu item added successfully\",\"item\":%s}",
                    menuItemToJson(newItem)
                );
                sendResponse(exchange, 201, json);

                System.out.println("➕ Menu item added via API: " + name);

            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid menu item data: " + e.getMessage() + "\"}");
            }
        }

        private void handleUpdateMenuItem(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            int itemId = Integer.parseInt(getQueryParam(query, "itemId"));

            String body = readRequestBody(exchange);
            Map<String, String> params = parseJson(body);

            try {
                String name = params.get("name");
                String description = params.get("description");
                double price = Double.parseDouble(params.get("price"));
                String category = params.get("category");
                String imageUrl = params.getOrDefault("imageUrl", "");

                boolean success = menuService.updateMenuItem(itemId, name, description, price, category, imageUrl);

                if (success) {
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Menu item updated successfully\"}");
                    System.out.println("✏️ Menu item updated via API: " + name + " (ID: " + itemId + ")");
                } else {
                    sendResponse(exchange, 404, "{\"success\":false,\"message\":\"Menu item not found\"}");
                }

            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid menu item data: " + e.getMessage() + "\"}");
            }
        }

        private void handleDeleteMenuItem(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            int itemId = Integer.parseInt(getQueryParam(query, "itemId"));

            boolean success = menuService.deleteMenuItem(itemId);

            if (success) {
                sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Menu item deleted successfully\"}");
                System.out.println("🗑️ Menu item deleted via API: ID " + itemId);
            } else {
                sendResponse(exchange, 404, "{\"success\":false,\"message\":\"Menu item not found\"}");
            }
        }
    }

    // Cart API handler
    class CartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            String method = exchange.getRequestMethod();
            String customerId = getCustomerId(exchange);
            
            try {
                switch (method) {
                    case "GET":
                        handleGetCart(exchange, customerId);
                        break;
                    case "POST":
                        handleAddToCart(exchange, customerId);
                        break;
                    case "PUT":
                        handleUpdateCart(exchange, customerId);
                        break;
                    case "DELETE":
                        handleRemoveFromCart(exchange, customerId);
                        break;
                    default:
                        sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
        
        private void handleGetCart(HttpExchange exchange, String customerId) throws IOException {
            List<CartItem> items = cartService.getCartItems(customerId);
            double subtotal = cartService.getCartSubtotal(customerId);
            int itemCount = cartService.getCartItemCount(customerId);
            
            String json = String.format(
                "{\"items\":%s,\"subtotal\":%.2f,\"itemCount\":%d}",
                cartItemsToJson(items), subtotal, itemCount
            );
            
            sendResponse(exchange, 200, json);
            
            // Display in CLI
            System.out.println("🛒 Cart requested for customer: " + customerId + " (" + itemCount + " items)");
        }
        
        private void handleAddToCart(HttpExchange exchange, String customerId) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseJson(body);
            
            int itemId = Integer.parseInt(params.get("itemId"));
            int quantity = Integer.parseInt(params.get("quantity"));
            String instructions = params.getOrDefault("instructions", "");
            
            boolean success = cartService.addItemToCart(customerId, itemId, quantity, instructions);
            
            if (success) {
                sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Item added to cart\"}");
                
                // Display in CLI
                Optional<MenuItem> item = menuService.getMenuItemById(itemId);
                System.out.println("➕ Added to cart: " + (item.isPresent() ? item.get().getName() : "Item " + itemId) + 
                                 " (qty: " + quantity + ") for customer: " + customerId);
                cartService.displayCart(customerId);
            } else {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to add item to cart\"}");
            }
        }
        
        private void handleUpdateCart(HttpExchange exchange, String customerId) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseJson(body);
            
            int itemId = Integer.parseInt(params.get("itemId"));
            int quantity = Integer.parseInt(params.get("quantity"));
            
            boolean success = cartService.updateCartItemQuantity(customerId, itemId, quantity);
            
            if (success) {
                sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Cart updated\"}");
                System.out.println("🔄 Cart updated for customer: " + customerId);
            } else {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to update cart\"}");
            }
        }
        
        private void handleRemoveFromCart(HttpExchange exchange, String customerId) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            int itemId = Integer.parseInt(getQueryParam(query, "itemId"));
            
            boolean success = cartService.removeItemFromCart(customerId, itemId);
            
            if (success) {
                sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Item removed from cart\"}");
                System.out.println("➖ Item removed from cart for customer: " + customerId);
            } else {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to remove item\"}");
            }
        }
    }

    // Order API handler
    class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            String method = exchange.getRequestMethod();
            String customerId = getCustomerId(exchange);
            
            try {
                switch (method) {
                    case "GET":
                        handleGetOrders(exchange, customerId);
                        break;
                    case "POST":
                        handleCreateOrder(exchange, customerId);
                        break;
                    default:
                        sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
        
        private void handleGetOrders(HttpExchange exchange, String customerId) throws IOException {
            List<Order> orders = orderService.getOrdersByCustomer(customerId);
            String json = ordersToJson(orders);
            sendResponse(exchange, 200, json);
            
            System.out.println("📋 Orders requested for customer: " + customerId + " (" + orders.size() + " orders)");
        }
        
        private void handleCreateOrder(HttpExchange exchange, String customerId) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseJson(body);
            
            String deliveryType = params.getOrDefault("deliveryType", "standard");
            String address = params.getOrDefault("address", "");
            String phone = params.getOrDefault("phone", "");
            
            Order order = orderService.createOrderFromCart(customerId, deliveryType, address, phone);
            
            if (order != null) {
                String json = String.format(
                    "{\"success\":true,\"orderId\":\"%s\",\"total\":%.2f,\"message\":\"Order created successfully\"}",
                    order.getOrderId(), order.getTotalAmount()
                );
                sendResponse(exchange, 200, json);
                
                // Display in CLI
                System.out.println("🎉 NEW ORDER CREATED!");
                orderService.displayOrder(order.getOrderId());
            } else {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to create order\"}");
            }
        }
    }

    // Utility methods
    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Content-Type", "application/json");
    }

    private String getCustomerId(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        return getQueryParam(query, "customerId", "web-customer-1");
    }

    private String getQueryParam(String query, String param) {
        return getQueryParam(query, param, "");
    }

    private String getQueryParam(String query, String param, String defaultValue) {
        if (query == null) return defaultValue;
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(param)) {
                return keyValue[1];
            }
        }
        return defaultValue;
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        }
    }

    private Map<String, String> parseJson(String json) {
        Map<String, String> result = new HashMap<>();
        
        // Simple JSON parsing (for basic key-value pairs)
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
            String[] pairs = json.split(",");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("\"", "");
                    String value = keyValue[1].trim().replaceAll("\"", "");
                    result.put(key, value);
                }
            }
        }
        
        return result;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    // JSON conversion methods
    private String menuItemsToJson(List<MenuItem> items) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            json.append(String.format(
                "{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"category\":\"%s\",\"image\":\"%s\",\"available\":%s}",
                item.getId(), escapeJson(item.getName()), escapeJson(item.getDescription()), 
                item.getPrice(), item.getCategory(), escapeJson(item.getImageUrl()), item.isAvailable()
            ));
            if (i < items.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }

    private String cartItemsToJson(List<CartItem> items) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            MenuItem menuItem = item.getMenuItem();
            json.append(String.format(
                "{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"category\":\"%s\",\"image\":\"%s\",\"quantity\":%d,\"totalPrice\":%.2f,\"instructions\":\"%s\"}",
                menuItem.getId(), escapeJson(menuItem.getName()), escapeJson(menuItem.getDescription()),
                menuItem.getPrice(), menuItem.getCategory(), escapeJson(menuItem.getImageUrl()),
                item.getQuantity(), item.getTotalPrice(), escapeJson(item.getSpecialInstructions())
            ));
            if (i < items.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }

    private String ordersToJson(List<Order> orders) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            json.append(String.format(
                "{\"orderId\":\"%s\",\"customerId\":\"%s\",\"status\":\"%s\",\"totalAmount\":%.2f,\"orderTime\":\"%s\",\"deliveryType\":\"%s\",\"itemCount\":%d}",
                order.getOrderId(), order.getCustomerId(), order.getStatus().toString(),
                order.getTotalAmount(), order.getOrderTime().toString(),
                order.getDeliveryType() != null ? order.getDeliveryType() : "standard",
                order.getTotalItemCount()
            ));
            if (i < orders.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    // Single menu item to JSON
    private String menuItemToJson(MenuItem item) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"category\":\"%s\",\"image\":\"%s\",\"available\":%s}",
            item.getId(), escapeJson(item.getName()), escapeJson(item.getDescription()),
            item.getPrice(), item.getCategory(), escapeJson(item.getImageUrl()), item.isAvailable()
        );
    }
}
