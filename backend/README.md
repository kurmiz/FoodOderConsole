# FoodieExpress Backend - Java CLI Application

A comprehensive Java backend for the FoodieExpress food ordering system with in-memory storage and command-line interface.

## Features

### 🍕 **Menu Management**
- Pre-loaded menu with 12 gourmet items across 4 categories
- Add, update, and manage menu items
- Category-based filtering and search functionality
- Toggle item availability
- Menu statistics and analytics

### 🛒 **Cart Operations**
- Add items to cart with quantities and special instructions
- Update item quantities and remove items
- Multiple customer cart support
- Cart validation before checkout
- Real-time cart calculations

### 📦 **Order Processing**
- Convert cart to orders with delivery options
- Order status tracking (Pending → Confirmed → Preparing → On the Way → Delivered)
- Order history and customer order tracking
- Order cancellation (before delivery)
- Comprehensive order statistics

### 👥 **Customer Management**
- Multi-customer support with unique customer IDs
- Customer-specific cart and order history
- Quick order functionality for popular items
- Customer information and statistics

### 🔧 **Admin Operations**
- View all orders and filter by status
- Update order statuses
- View pending orders for kitchen operations
- System-wide statistics and analytics
- Menu item management

## Project Structure

```
backend/
├── src/main/java/com/foodieexpress/
│   ├── model/
│   │   ├── MenuItem.java          # Menu item entity
│   │   ├── CartItem.java          # Cart item with quantity
│   │   ├── Cart.java              # Customer shopping cart
│   │   └── Order.java             # Order with status tracking
│   ├── service/
│   │   ├── MenuService.java       # Menu operations
│   │   ├── CartService.java       # Cart management
│   │   └── OrderService.java      # Order processing
│   └── FoodieExpressApp.java      # Main CLI application
├── compile.bat                    # Windows compilation script
├── compile.sh                     # Unix compilation script
├── run.bat                        # Windows run script
├── run.sh                         # Unix run script
└── README.md                      # This file
```

## Getting Started

### Prerequisites
- Java 8 or higher
- Command line access

### Compilation

**Windows:**
```bash
compile.bat
```

**Unix/Linux/Mac:**
```bash
chmod +x compile.sh
./compile.sh
```

### Running the Application

**Windows:**
```bash
run.bat
```

**Unix/Linux/Mac:**
```bash
chmod +x run.sh
./run.sh
```

**Manual execution:**
```bash
java -cp out com.foodieexpress.FoodieExpressApp
```

## Usage Guide

### Main Menu Options

1. **Menu Operations**
   - View complete menu with categories
   - Search for specific items
   - Filter by category (pizza, burger, pasta, dessert)
   - Add items directly to cart

2. **Cart Operations**
   - View current cart contents
   - Add/remove items
   - Update quantities
   - Proceed to checkout

3. **Order Operations**
   - View order history
   - Track order status
   - Cancel pending orders
   - View order details

4. **Customer Operations**
   - Switch between customers
   - Quick order popular items
   - View customer information

5. **Admin Operations**
   - Manage all orders
   - Update order statuses
   - View system statistics
   - Manage menu items

### Sample Workflow

1. **Browse Menu:**
   ```
   Main Menu → Menu Operations → View All Menu Items
   ```

2. **Add Items to Cart:**
   ```
   Menu Operations → Add Item to Cart
   Enter Item ID: 1
   Enter Quantity: 2
   Enter Special Instructions: Extra cheese
   ```

3. **Checkout:**
   ```
   Main Menu → Cart Operations → Proceed to Checkout
   Delivery Type: express
   Address: 123 Main St
   Phone: 555-1234
   ```

4. **Track Order:**
   ```
   Main Menu → Order Operations → Track Order Status
   Enter Order ID: 1001
   ```

## Data Models

### MenuItem
- ID, name, description, price
- Category and availability status
- Image URL for frontend integration

### CartItem
- Menu item reference with quantity
- Special instructions
- Calculated total price

### Order
- Unique order ID and customer ID
- Order items with quantities
- Subtotal, delivery fee, tax, total
- Order status and timestamps
- Delivery information

## In-Memory Storage

All data is stored in memory using Java collections:
- **ArrayList** for menu items and orders
- **HashMap** for customer carts
- **EnumMap** for order status tracking

Data persists only during application runtime and is reset on restart.

## CLI Features

### Interactive Menus
- Numbered menu options for easy navigation
- Input validation and error handling
- Formatted output with borders and spacing

### Data Display
- Tabular format for menu items
- Detailed order summaries
- Real-time statistics and analytics

### Error Handling
- Invalid input validation
- Graceful error messages
- Retry mechanisms for user input

## Sample Data

The application comes pre-loaded with 12 gourmet menu items:

**Pizza (3 items):**
- Margherita Pizza - $16.99
- Pepperoni Supreme - $19.99
- Truffle Mushroom Pizza - $24.99

**Burgers (3 items):**
- Gourmet Beef Burger - $18.99
- BBQ Bacon Burger - $16.99
- Chicken Avocado Burger - $15.99

**Pasta (3 items):**
- Lobster Ravioli - $28.99
- Truffle Carbonara - $22.99
- Seafood Linguine - $26.99

**Desserts (3 items):**
- Chocolate Lava Cake - $9.99
- Classic Tiramisu - $8.99
- Crème Brûlée - $10.99

## Future Enhancements

- Database integration (MySQL/PostgreSQL)
- REST API endpoints
- User authentication and authorization
- Payment processing integration
- Real-time notifications
- Inventory management
- Delivery tracking with GPS
- Customer reviews and ratings

## License

This project is open source and available under the MIT License.
