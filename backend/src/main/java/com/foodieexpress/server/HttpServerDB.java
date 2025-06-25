package com.foodieexpress.server;

import com.foodieexpress.service.MenuServiceDB;
import com.foodieexpress.service.CartServiceDB;
import com.foodieexpress.service.OrderServiceDB;
import com.foodieexpress.model.MenuItem;
import com.foodieexpress.model.CartItem;
import com.foodieexpress.model.Order;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Database-backed HTTP Server to serve the frontend and provide REST API endpoints
 */
public class HttpServerDB {
    private final com.sun.net.httpserver.HttpServer server;
    private final MenuServiceDB menuService;
    private final CartServiceDB cartService;
    private final OrderServiceDB orderService;
    private final int port;

    public HttpServerDB(int port, MenuServiceDB menuService, CartServiceDB cartService, OrderServiceDB orderService) throws IOException {
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

        // Static file handler (must be last)
        server.createContext("/", new StaticFileHandler());
        
        server.setExecutor(null); // Use default executor
    }

    public void start() {
        server.start();
        System.out.println("🌐 FoodieExpress Server started on http://localhost:" + port);
        System.out.println("📱 Frontend: http://localhost:" + port);
        System.out.println("🔗 API Base: http://localhost:" + port + "/api");
    }

    public void stop() {
        server.stop(0);
        System.out.println("🛑 Server stopped");
    }

    // Menu API Handler
    private class MenuHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            
            System.out.println("🔗 API Request: " + method + " " + path);
            
            // Add CORS headers
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            
            if ("OPTIONS".equals(method)) {
                sendResponse(exchange, 200, "");
                return;
            }

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
                System.err.println("❌ Error handling menu request: " + e.getMessage());
                sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
            }
        }

        private void handleGetMenu(HttpExchange exchange) throws IOException {
            List<MenuItem> menuItems = menuService.getAllMenuItems();
            String jsonResponse = menuItemsToJson(menuItems);
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, jsonResponse);
            
            System.out.println("📋 Menu requested via API - " + menuItems.size() + " items served");
        }

        private void handleAddMenuItem(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseRequestBody(exchange);
            
            String name = params.get("name");
            String description = params.get("description");
            String priceStr = params.get("price");
            String category = params.get("category");
            String imageUrl = params.getOrDefault("imageUrl", "");

            if (name == null || description == null || priceStr == null || category == null) {
                sendResponse(exchange, 400, "{\"error\":\"Missing required fields\"}");
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                MenuItem newItem = menuService.addMenuItem(name, description, price, category, imageUrl);
                
                if (newItem != null) {
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Menu item added successfully\",\"id\":" + newItem.getId() + "}");
                    System.out.println("✅ Menu item added via API: " + name);
                } else {
                    sendResponse(exchange, 500, "{\"error\":\"Failed to add menu item\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid price format\"}");
            }
        }

        private void handleUpdateMenuItem(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseRequestBody(exchange);
            String query = exchange.getRequestURI().getQuery();
            
            if (query == null || !query.startsWith("itemId=")) {
                sendResponse(exchange, 400, "{\"error\":\"Missing itemId parameter\"}");
                return;
            }

            try {
                int itemId = Integer.parseInt(query.substring(7));
                String name = params.get("name");
                String description = params.get("description");
                String priceStr = params.get("price");
                String category = params.get("category");
                String imageUrl = params.getOrDefault("imageUrl", "");

                double price = priceStr != null ? Double.parseDouble(priceStr) : 0;
                
                boolean success = menuService.updateMenuItem(itemId, name, description, price, category, imageUrl);

                if (success) {
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Menu item updated successfully\"}");
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Menu item not found\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid itemId or price format\"}");
            }
        }

        private void handleDeleteMenuItem(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            
            if (query == null || !query.startsWith("itemId=")) {
                sendResponse(exchange, 400, "{\"error\":\"Missing itemId parameter\"}");
                return;
            }

            try {
                int itemId = Integer.parseInt(query.substring(7));
                boolean success = menuService.deleteMenuItem(itemId);

                if (success) {
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Menu item deleted successfully\"}");
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Menu item not found\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid itemId format\"}");
            }
        }
    }

    // Cart API Handler
    private class CartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            // Add CORS headers
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            
            if ("OPTIONS".equals(method)) {
                sendResponse(exchange, 200, "");
                return;
            }

            try {
                switch (method) {
                    case "GET":
                        handleGetCart(exchange);
                        break;
                    case "POST":
                        handleAddToCart(exchange);
                        break;
                    case "PUT":
                        handleUpdateCart(exchange);
                        break;
                    case "DELETE":
                        handleRemoveFromCart(exchange);
                        break;
                    default:
                        sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception e) {
                System.err.println("❌ Error handling cart request: " + e.getMessage());
                sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
            }
        }

        private void handleGetCart(HttpExchange exchange) throws IOException {
            String customerId = getCustomerIdFromQuery(exchange);
            Map<String, Object> cartData = cartService.getCartData(customerId);
            
            String jsonResponse = cartDataToJson(cartData);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, jsonResponse);
            
            System.out.println("🛒 Cart requested for customer: " + customerId + " (" + cartService.getCartItemCount(customerId) + " items)");
        }

        private void handleAddToCart(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseRequestBody(exchange);
            String customerId = params.getOrDefault("customerId", "web-customer-1");
            
            try {
                int itemId = Integer.parseInt(params.get("itemId"));
                int quantity = Integer.parseInt(params.getOrDefault("quantity", "1"));
                String instructions = params.getOrDefault("specialInstructions", "");
                
                boolean success = cartService.addItemToCart(customerId, itemId, quantity, instructions);
                
                if (success) {
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Item added to cart\"}");
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"Failed to add item to cart\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid itemId or quantity\"}");
            }
        }

        private void handleUpdateCart(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseRequestBody(exchange);
            String customerId = params.getOrDefault("customerId", "web-customer-1");
            
            try {
                int itemId = Integer.parseInt(params.get("itemId"));
                int quantity = Integer.parseInt(params.get("quantity"));
                
                boolean success = cartService.updateCartItemQuantity(customerId, itemId, quantity);
                
                if (success) {
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Cart updated\"}");
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"Failed to update cart\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid itemId or quantity\"}");
            }
        }

        private void handleRemoveFromCart(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQuery(query);
            
            String customerId = queryParams.getOrDefault("customerId", "web-customer-1");
            
            try {
                int itemId = Integer.parseInt(queryParams.get("itemId"));
                boolean success = cartService.removeItemFromCart(customerId, itemId);
                
                if (success) {
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Item removed from cart\"}");
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"Failed to remove item from cart\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid itemId\"}");
            }
        }
    }

    // Orders API Handler
    private class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            // Add CORS headers
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            
            if ("OPTIONS".equals(method)) {
                sendResponse(exchange, 200, "");
                return;
            }

            try {
                switch (method) {
                    case "GET":
                        handleGetOrders(exchange);
                        break;
                    case "POST":
                        handleCreateOrder(exchange);
                        break;
                    case "PUT":
                        handleUpdateOrder(exchange);
                        break;
                    default:
                        sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception e) {
                System.err.println("❌ Error handling order request: " + e.getMessage());
                sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
            }
        }

        private void handleGetOrders(HttpExchange exchange) throws IOException {
            String customerId = getCustomerIdFromQuery(exchange);
            List<Order> orders = orderService.getOrdersByCustomer(customerId);
            
            String jsonResponse = ordersToJson(orders);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, jsonResponse);
            
            System.out.println("📦 Orders requested for customer: " + customerId + " (" + orders.size() + " orders)");
        }

        private void handleCreateOrder(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseRequestBody(exchange);
            String customerId = params.getOrDefault("customerId", "web-customer-1");
            String deliveryType = params.getOrDefault("deliveryType", "standard");
            String address = params.getOrDefault("address", "123 Web Street");
            String phone = params.getOrDefault("phone", "555-WEB-USER");

            Order order = orderService.createOrderFromCart(customerId, deliveryType, address, phone);

            if (order != null) {
                String jsonResponse = orderToJson(order);
                sendResponse(exchange, 200, jsonResponse);
                System.out.println("📦 Order created via API: " + order.getOrderId() + " for customer: " + customerId);
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Failed to create order - cart may be empty\"}");
            }
        }

        private void handleUpdateOrder(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseRequestBody(exchange);
            String orderId = params.get("orderId");
            String statusStr = params.get("status");
            
            if (orderId == null || statusStr == null) {
                sendResponse(exchange, 400, "{\"error\":\"Missing orderId or status\"}");
                return;
            }
            
            try {
                Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr);
                boolean success = orderService.updateOrderStatus(orderId, status);
                
                if (success) {
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Order status updated\"}");
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Order not found\"}");
                }
            } catch (IllegalArgumentException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid status\"}");
            }
        }
    }

    // Static file handler for serving frontend
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Default to index.html for root path
            if ("/".equals(path)) {
                path = "/index.html";
            }
            
            File file = new File("../frontend" + path);
            
            if (file.exists() && file.isFile()) {
                String contentType = getContentType(path);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                exchange.sendResponseHeaders(200, fileBytes.length);
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(fileBytes);
                }
            } else {
                // File not found
                String notFound = "404 - File not found: " + path;
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes());
                }
            }
        }
    }

    // Helper methods
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private Map<String, String> parseRequestBody(HttpExchange exchange) throws IOException {
        Map<String, String> params = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            
            String bodyStr = body.toString();
            if (bodyStr.startsWith("{") && bodyStr.endsWith("}")) {
                // Simple JSON parsing
                bodyStr = bodyStr.substring(1, bodyStr.length() - 1);
                String[] pairs = bodyStr.split(",");
                
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replaceAll("\"", "");
                        String value = keyValue[1].trim().replaceAll("\"", "");
                        params.put(key, value);
                    }
                }
            }
        }
        
        return params;
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    try {
                        String key = URLDecoder.decode(keyValue[0], "UTF-8");
                        String value = URLDecoder.decode(keyValue[1], "UTF-8");
                        params.put(key, value);
                    } catch (UnsupportedEncodingException e) {
                        // Ignore malformed parameters
                    }
                }
            }
        }
        return params;
    }

    private String getCustomerIdFromQuery(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        return params.getOrDefault("customerId", "web-customer-1");
    }

    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".svg")) return "image/svg+xml";
        return "text/plain";
    }

    // JSON conversion methods (simplified)
    private String menuItemsToJson(List<MenuItem> items) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) json.append(",");
            json.append(menuItemToJson(items.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    private String menuItemToJson(MenuItem item) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"category\":\"%s\",\"imageUrl\":\"%s\",\"available\":%b}",
            item.getId(), escapeJson(item.getName()), escapeJson(item.getDescription()),
            item.getPrice(), escapeJson(item.getCategory()), escapeJson(item.getImageUrl()), item.isAvailable()
        );
    }

    private String cartDataToJson(Map<String, Object> cartData) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"customerId\":\"").append(cartData.get("customerId")).append("\",");
        json.append("\"itemCount\":").append(cartData.get("itemCount")).append(",");
        json.append("\"subtotal\":").append(cartData.get("subtotal")).append(",");
        json.append("\"isEmpty\":").append(cartData.get("isEmpty")).append(",");
        json.append("\"lastUpdated\":").append(cartData.get("lastUpdated")).append(",");

        // Add cart items array
        @SuppressWarnings("unchecked")
        List<CartItem> items = (List<CartItem>) cartData.get("items");
        json.append("\"items\":[");

        for (int i = 0; i < items.size(); i++) {
            if (i > 0) json.append(",");
            CartItem item = items.get(i);
            json.append("{");
            json.append("\"id\":").append(item.getMenuItem().getId()).append(",");
            json.append("\"name\":\"").append(escapeJson(item.getMenuItem().getName())).append("\",");
            json.append("\"price\":").append(item.getMenuItem().getPrice()).append(",");
            json.append("\"quantity\":").append(item.getQuantity()).append(",");
            json.append("\"totalPrice\":").append(item.getTotalPrice()).append(",");
            json.append("\"specialInstructions\":\"").append(escapeJson(item.getSpecialInstructions())).append("\",");
            json.append("\"imageUrl\":\"").append(escapeJson(item.getMenuItem().getImageUrl())).append("\"");
            json.append("}");
        }

        json.append("]}");
        return json.toString();
    }

    private String ordersToJson(List<Order> orders) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) json.append(",");
            json.append(orderToJson(orders.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    private String orderToJson(Order order) {
        return String.format(
            "{\"orderId\":\"%s\",\"customerId\":\"%s\",\"status\":\"%s\",\"totalAmount\":%.2f,\"orderTime\":\"%s\"}",
            order.getOrderId(), order.getCustomerId(), order.getStatus(),
            order.getTotalAmount(), order.getOrderTime().toString()
        );
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
