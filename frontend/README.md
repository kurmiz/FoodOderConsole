# FoodieExpress - Food Ordering App

A modern, responsive food ordering web application built with HTML, CSS, and JavaScript.

## Features

### 🍕 Menu Display
- Dynamic menu with categorized food items
- Filter by categories (Pizza, Burgers, Pasta, Desserts)
- High-quality food images with fallback placeholders
- Responsive grid layout

### 🛒 Shopping Cart
- Add items to cart with quantity management
- Sliding cart sidebar with smooth animations
- Real-time cart count and total calculation
- Increase/decrease item quantities
- Remove items from cart

### 🎨 User Interface
- Modern, clean design with attractive color scheme
- Responsive layout that works on all devices
- Smooth scrolling navigation
- Interactive buttons with hover effects
- Professional typography and spacing

### 📱 Responsive Design
- Mobile-first approach
- Optimized for tablets and desktops
- Collapsible navigation on smaller screens
- Touch-friendly interface elements

## File Structure

```
frontend/
├── index.html          # Main HTML file
├── styles.css          # CSS styling
├── script.js           # JavaScript functionality
└── README.md          # Project documentation
```

## Getting Started

1. **Open the application:**
   - Simply open `index.html` in your web browser
   - No server setup required - it's a static web application

2. **Browse the menu:**
   - Scroll down to the menu section or click "Order Now"
   - Use category filters to find specific types of food
   - View item details including descriptions and prices

3. **Add items to cart:**
   - Click "Add to Cart" on any menu item
   - Click the cart icon to view your order
   - Adjust quantities using + and - buttons

4. **Place an order:**
   - Review your cart items and total
   - Click "Checkout" to complete your order

## Technical Details

### HTML Structure
- Semantic HTML5 elements
- Accessible navigation and form elements
- Meta tags for responsive design
- Font Awesome icons for visual elements

### CSS Features
- CSS Grid and Flexbox for layouts
- CSS transitions and animations
- Custom properties for consistent theming
- Media queries for responsive design
- Modern CSS techniques (box-shadow, border-radius)

### JavaScript Functionality
- ES6+ features (arrow functions, template literals, destructuring)
- DOM manipulation and event handling
- Local state management for cart functionality
- Smooth scrolling and UI interactions
- Dynamic content rendering

## Menu Items

The app includes sample menu items across four categories:

- **Pizza:** Margherita, Pepperoni
- **Burgers:** Classic Burger, Cheese Burger
- **Pasta:** Spaghetti Carbonara, Penne Arrabbiata
- **Desserts:** Chocolate Cake, Tiramisu

## Customization

### Adding New Menu Items
Edit the `menuItems` array in `script.js`:

```javascript
{
    id: 9,
    name: "New Item",
    description: "Item description",
    price: 9.99,
    category: "category",
    image: "image-url"
}
```

### Styling Changes
Modify `styles.css` to change:
- Color scheme (update CSS custom properties)
- Typography (change font families)
- Layout (adjust grid and flexbox properties)
- Animations (modify transition properties)

### Adding Features
Extend `script.js` to add:
- User authentication
- Order history
- Payment integration
- Delivery tracking
- Restaurant selection

## Browser Compatibility

- Chrome 60+
- Firefox 55+
- Safari 12+
- Edge 79+

## Future Enhancements

- Backend integration for real orders
- User accounts and order history
- Payment gateway integration
- Real-time order tracking
- Restaurant management system
- Reviews and ratings
- Delivery time estimation
- Multiple restaurant support

## License

This project is open source and available under the MIT License.
