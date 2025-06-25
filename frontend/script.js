// Menu data
const menuItems = [
    {
        id: 1,
        name: "Margherita Pizza",
        description: "Fresh San Marzano tomatoes, buffalo mozzarella, fresh basil, extra virgin olive oil",
        price: 16.99,
        category: "pizza",
        image: "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 2,
        name: "Pepperoni Supreme",
        description: "Premium pepperoni, mozzarella cheese, rich tomato sauce, oregano",
        price: 19.99,
        category: "pizza",
        image: "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 3,
        name: "Truffle Mushroom Pizza",
        description: "Wild mushrooms, truffle oil, mozzarella, arugula, parmesan shavings",
        price: 24.99,
        category: "pizza",
        image: "https://images.unsplash.com/photo-1513104890138-7c749659a591?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 4,
        name: "Gourmet Beef Burger",
        description: "Wagyu beef patty, aged cheddar, caramelized onions, truffle aioli, brioche bun",
        price: 18.99,
        category: "burger",
        image: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 5,
        name: "BBQ Bacon Burger",
        description: "Beef patty, crispy bacon, BBQ sauce, onion rings, cheddar cheese",
        price: 16.99,
        category: "burger",
        image: "https://images.unsplash.com/photo-1571091718767-18b5b1457add?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 6,
        name: "Chicken Avocado Burger",
        description: "Grilled chicken breast, fresh avocado, lettuce, tomato, herb mayo",
        price: 15.99,
        category: "burger",
        image: "https://images.unsplash.com/photo-1606755962773-d324e9a13086?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 7,
        name: "Lobster Ravioli",
        description: "Fresh lobster ravioli in creamy saffron sauce with cherry tomatoes",
        price: 28.99,
        category: "pasta",
        image: "https://images.unsplash.com/photo-1551183053-bf91a1d81141?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 8,
        name: "Truffle Carbonara",
        description: "Fresh pasta with eggs, pecorino romano, pancetta, black truffle shavings",
        price: 22.99,
        category: "pasta",
        image: "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 9,
        name: "Seafood Linguine",
        description: "Fresh linguine with shrimp, scallops, mussels in white wine garlic sauce",
        price: 26.99,
        category: "pasta",
        image: "https://images.unsplash.com/photo-1551892374-ecf8754cf8b0?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 10,
        name: "Chocolate Lava Cake",
        description: "Warm chocolate cake with molten center, vanilla ice cream, berry compote",
        price: 9.99,
        category: "dessert",
        image: "https://images.unsplash.com/photo-1578985545062-69928b1d9587?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 11,
        name: "Classic Tiramisu",
        description: "Traditional Italian dessert with mascarpone, coffee, cocoa, ladyfingers",
        price: 8.99,
        category: "dessert",
        image: "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
    },
    {
        id: 12,
        name: "Crème Brûlée",
        description: "Vanilla custard with caramelized sugar crust, fresh berries",
        price: 10.99,
        category: "dessert",
        image: "https://images.unsplash.com/photo-1470324161839-ce2bb6fa6bc3?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
    }
];

// Cart functionality
let cart = [];
let orders = [];
let currentSection = 'home';

// DOM elements
const menuGrid = document.getElementById('menuGrid');
const cartSidebar = document.getElementById('cartSidebar');
const cartOverlay = document.getElementById('cartOverlay');
const cartItems = document.getElementById('cartItems');
const cartCount = document.getElementById('cartCount');
const cartTotal = document.getElementById('cartTotal');

// Initialize the app
document.addEventListener('DOMContentLoaded', function() {
    displayMenuItems(menuItems);
    displayFeaturedItems();
    displayQuickAddItems();
    updateCartDisplay();
    updateCartPageDisplay();
    loadOrderHistory();
    showSection('home');
});

// Section navigation
function showSection(sectionName) {
    // Hide all sections
    document.querySelectorAll('.section-page').forEach(section => {
        section.classList.remove('active');
    });

    // Show selected section
    document.getElementById(sectionName).classList.add('active');
    currentSection = sectionName;

    // Update navigation
    document.querySelectorAll('.nav a').forEach(link => {
        link.classList.remove('active');
    });
    document.querySelector(`[href="#${sectionName}"]`).classList.add('active');

    // Close cart if open
    if (cartSidebar.classList.contains('open')) {
        toggleCart();
    }

    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Display featured items on home page
function displayFeaturedItems() {
    const featuredGrid = document.getElementById('featuredGrid');
    if (!featuredGrid) return;

    // Get 3 featured items (one from each category)
    const featured = [
        menuItems.find(item => item.category === 'pizza'),
        menuItems.find(item => item.category === 'burger'),
        menuItems.find(item => item.category === 'pasta')
    ];

    featuredGrid.innerHTML = '';

    featured.forEach(item => {
        if (item) {
            const featuredItem = document.createElement('div');
            featuredItem.className = 'featured-item';

            featuredItem.innerHTML = `
                <img src="${item.image}" alt="${item.name}" onerror="this.src='data:image/svg+xml,<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 400 300\"><rect fill=\"%23f0f0f0\" width=\"400\" height=\"300\"/><text x=\"50%\" y=\"50%\" text-anchor=\"middle\" dy=\".3em\" fill=\"%23999\" font-family=\"Arial\" font-size=\"16\">Food Image</text></svg>'">
                <div class="featured-content">
                    <h3>${item.name}</h3>
                    <p>${item.description}</p>
                    <div class="featured-footer">
                        <span class="price">$${item.price.toFixed(2)}</span>
                        <button class="add-to-cart" onclick="addToCart(${item.id})">
                            Add to Cart
                        </button>
                    </div>
                </div>
            `;

            featuredGrid.appendChild(featuredItem);
        }
    });
}

// Display menu items
function displayMenuItems(items) {
    menuGrid.innerHTML = '';
    
    items.forEach(item => {
        const menuItemElement = document.createElement('div');
        menuItemElement.className = 'menu-item';
        menuItemElement.setAttribute('data-category', item.category);
        
        menuItemElement.innerHTML = `
            <img src="${item.image}" alt="${item.name}" onerror="this.src='data:image/svg+xml,<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 400 300\"><rect fill=\"%23f0f0f0\" width=\"400\" height=\"300\"/><text x=\"50%\" y=\"50%\" text-anchor=\"middle\" dy=\".3em\" fill=\"%23999\" font-family=\"Arial\" font-size=\"16\">Food Image</text></svg>'">
            <div class="menu-item-content">
                <h3>${item.name}</h3>
                <p>${item.description}</p>
                <div class="menu-item-footer">
                    <span class="price">$${item.price.toFixed(2)}</span>
                    <button class="add-to-cart" onclick="addToCart(${item.id})">
                        Add to Cart
                    </button>
                </div>
            </div>
        `;
        
        menuGrid.appendChild(menuItemElement);
    });
}

// Filter menu items
function filterMenu(category) {
    // Update active filter button
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // Filter and display items
    if (category === 'all') {
        displayMenuItems(menuItems);
    } else {
        const filteredItems = menuItems.filter(item => item.category === category);
        displayMenuItems(filteredItems);
    }
}

// Add item to cart
function addToCart(itemId) {
    const item = menuItems.find(item => item.id === itemId);
    const existingItem = cart.find(cartItem => cartItem.id === itemId);

    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({
            ...item,
            quantity: 1
        });
    }

    updateCartDisplay();
    updateCartPageDisplay();

    // Show feedback with improved animation
    const button = event.target;
    const originalText = button.textContent;
    const originalBackground = button.style.background;

    button.textContent = '✓ Added!';
    button.style.background = 'linear-gradient(135deg, #10b981, #059669)';
    button.style.transform = 'scale(0.95)';

    setTimeout(() => {
        button.textContent = originalText;
        button.style.background = originalBackground;
        button.style.transform = 'scale(1)';
    }, 1200);

    // Add subtle cart icon animation
    const cartIcon = document.querySelector('.cart-icon');
    cartIcon.style.transform = 'scale(1.1)';
    setTimeout(() => {
        cartIcon.style.transform = 'scale(1)';
    }, 200);
}

// Update cart display (sidebar)
function updateCartDisplay() {
    // Update cart count
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    cartCount.textContent = totalItems;

    // Update cart items
    cartItems.innerHTML = '';

    if (cart.length === 0) {
        cartItems.innerHTML = '<p style="text-align: center; color: #666; padding: 2rem;">Your cart is empty</p>';
    } else {
        cart.forEach(item => {
            const cartItemElement = document.createElement('div');
            cartItemElement.className = 'cart-item';

            cartItemElement.innerHTML = `
                <div class="cart-item-info">
                    <h4>${item.name}</h4>
                    <p>$${item.price.toFixed(2)} each</p>
                </div>
                <div class="quantity-controls">
                    <button class="quantity-btn" onclick="updateQuantity(${item.id}, -1)">-</button>
                    <span>${item.quantity}</span>
                    <button class="quantity-btn" onclick="updateQuantity(${item.id}, 1)">+</button>
                </div>
            `;

            cartItems.appendChild(cartItemElement);
        });
    }

    // Update total
    const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    cartTotal.textContent = total.toFixed(2);
}

// Update cart page display
function updateCartPageDisplay() {
    const cartPageItems = document.getElementById('cartPageItems');
    const cartSubtotal = document.getElementById('cartSubtotal');
    const cartTax = document.getElementById('cartTax');
    const cartPageTotal = document.getElementById('cartPageTotal');
    const deliveryFee = document.getElementById('deliveryFee');

    if (!cartPageItems) return;

    if (cart.length === 0) {
        cartPageItems.innerHTML = `
            <div class="empty-cart">
                <i class="fas fa-shopping-cart"></i>
                <h3>Your cart is empty</h3>
                <p>Add some delicious items to get started!</p>
                <button class="continue-shopping" onclick="showSection('menu')">
                    <i class="fas fa-utensils"></i> Browse Menu
                </button>
            </div>
        `;

        // Reset totals
        if (cartSubtotal) cartSubtotal.textContent = '$0.00';
        if (cartTax) cartTax.textContent = '$0.00';
        if (cartPageTotal) cartPageTotal.textContent = '$0.00';
        return;
    }

    // Display cart items
    cartPageItems.innerHTML = '';
    cart.forEach(item => {
        const cartPageItem = document.createElement('div');
        cartPageItem.className = 'cart-page-item';

        cartPageItem.innerHTML = `
            <img src="${item.image}" alt="${item.name}" class="cart-item-image"
                 onerror="this.src='data:image/svg+xml,<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><rect fill=\"%23f0f0f0\" width=\"100\" height=\"100\"/><text x=\"50%\" y=\"50%\" text-anchor=\"middle\" dy=\".3em\" fill=\"%23999\" font-size=\"12\">Food</text></svg>'">
            <div class="cart-item-details">
                <h4>${item.name}</h4>
                <p>${item.description || 'Delicious food item'}</p>
                <span class="cart-item-price">$${item.price.toFixed(2)} each</span>
            </div>
            <div class="cart-item-controls">
                <div class="quantity-controls">
                    <button class="quantity-btn" onclick="updateQuantity(${item.id}, -1)">-</button>
                    <span>${item.quantity}</span>
                    <button class="quantity-btn" onclick="updateQuantity(${item.id}, 1)">+</button>
                </div>
                <button class="remove-item" onclick="removeFromCart(${item.id})" title="Remove item">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;

        cartPageItems.appendChild(cartPageItem);
    });

    // Calculate totals
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const deliveryFeeValue = getDeliveryFee();
    const tax = subtotal * 0.08; // 8% tax
    const total = subtotal + deliveryFeeValue + tax;

    // Update totals
    if (cartSubtotal) cartSubtotal.textContent = `$${subtotal.toFixed(2)}`;
    if (deliveryFee) deliveryFee.textContent = `$${deliveryFeeValue.toFixed(2)}`;
    if (cartTax) cartTax.textContent = `$${tax.toFixed(2)}`;
    if (cartPageTotal) cartPageTotal.textContent = `$${total.toFixed(2)}`;
}

// Get delivery fee based on selected option
function getDeliveryFee() {
    const selectedDelivery = document.querySelector('input[name="delivery"]:checked');
    return selectedDelivery && selectedDelivery.value === 'express' ? 6.99 : 3.99;
}

// Remove item from cart
function removeFromCart(itemId) {
    cart = cart.filter(item => item.id !== itemId);
    updateCartDisplay();
    updateCartPageDisplay();
}

// Update item quantity in cart
function updateQuantity(itemId, change) {
    const item = cart.find(cartItem => cartItem.id === itemId);

    if (item) {
        item.quantity += change;

        if (item.quantity <= 0) {
            cart = cart.filter(cartItem => cartItem.id !== itemId);
        }

        updateCartDisplay();
        updateCartPageDisplay();
    }
}

// Display quick add items
function displayQuickAddItems() {
    const quickAddGrid = document.getElementById('quickAddGrid');
    if (!quickAddGrid) return;

    // Get popular items (first 6 items)
    const quickAddItems = menuItems.slice(0, 6);

    quickAddGrid.innerHTML = '';

    quickAddItems.forEach(item => {
        const quickAddItem = document.createElement('div');
        quickAddItem.className = 'quick-add-item';

        quickAddItem.innerHTML = `
            <h4>${item.name}</h4>
            <p>${item.description.substring(0, 50)}...</p>
            <div class="price">$${item.price.toFixed(2)}</div>
            <button onclick="addToCart(${item.id})">Quick Add</button>
        `;

        quickAddGrid.appendChild(quickAddItem);
    });
}

// Custom item builder
let customItem = {
    base: null,
    toppings: [],
    quantity: 1,
    instructions: ''
};

function updateCustomItem() {
    const itemType = document.getElementById('itemType').value;
    const quantity = parseInt(document.getElementById('quantity').value) || 1;
    const instructions = document.getElementById('specialInstructions').value;

    // Get selected toppings
    const toppings = [];
    document.querySelectorAll('.topping-item input[type="checkbox"]:checked').forEach(checkbox => {
        toppings.push({
            name: checkbox.nextElementSibling.textContent.split(' (+')[0],
            price: parseFloat(checkbox.value)
        });
    });

    customItem = {
        base: itemType,
        toppings: toppings,
        quantity: quantity,
        instructions: instructions
    };

    updateCustomPreview();
}

function updateCustomPreview() {
    const preview = document.getElementById('customPreview');
    const priceElement = document.getElementById('customPrice');
    const addButton = document.getElementById('addCustomBtn');

    if (!customItem.base) {
        preview.innerHTML = '<p>Select a base to start building your custom item</p>';
        priceElement.textContent = '0.00';
        addButton.disabled = true;
        return;
    }

    // Calculate price
    const basePrices = {
        'pizza': 12.99,
        'burger': 8.99,
        'pasta': 10.99,
        'salad': 7.99
    };

    const basePrice = basePrices[customItem.base] || 0;
    const toppingsPrice = customItem.toppings.reduce((sum, topping) => sum + topping.price, 0);
    const totalPrice = (basePrice + toppingsPrice) * customItem.quantity;

    // Update preview
    const baseNames = {
        'pizza': 'Custom Pizza',
        'burger': 'Custom Burger',
        'pasta': 'Custom Pasta',
        'salad': 'Custom Salad'
    };

    let previewHTML = `
        <div style="text-align: left;">
            <h5>${baseNames[customItem.base]}</h5>
            <p><strong>Base:</strong> ${baseNames[customItem.base]} ($${basePrice.toFixed(2)})</p>
    `;

    if (customItem.toppings.length > 0) {
        previewHTML += '<p><strong>Toppings:</strong></p><ul style="margin-left: 1rem;">';
        customItem.toppings.forEach(topping => {
            previewHTML += `<li>${topping.name} (+$${topping.price.toFixed(2)})</li>`;
        });
        previewHTML += '</ul>';
    }

    if (customItem.instructions) {
        previewHTML += `<p><strong>Instructions:</strong> ${customItem.instructions}</p>`;
    }

    previewHTML += `<p><strong>Quantity:</strong> ${customItem.quantity}</p>`;
    previewHTML += '</div>';

    preview.innerHTML = previewHTML;
    priceElement.textContent = totalPrice.toFixed(2);
    addButton.disabled = false;
}

function adjustCustomQuantity(change) {
    const quantityInput = document.getElementById('quantity');
    let newQuantity = parseInt(quantityInput.value) + change;

    if (newQuantity < 1) newQuantity = 1;
    if (newQuantity > 10) newQuantity = 10;

    quantityInput.value = newQuantity;
    updateCustomItem();
}

function addCustomItem() {
    if (!customItem.base) return;

    const basePrices = {
        'pizza': 12.99,
        'burger': 8.99,
        'pasta': 10.99,
        'salad': 7.99
    };

    const baseNames = {
        'pizza': 'Custom Pizza',
        'burger': 'Custom Burger',
        'pasta': 'Custom Pasta',
        'salad': 'Custom Salad'
    };

    const basePrice = basePrices[customItem.base];
    const toppingsPrice = customItem.toppings.reduce((sum, topping) => sum + topping.price, 0);
    const itemPrice = basePrice + toppingsPrice;

    // Create description
    let description = `Custom ${customItem.base}`;
    if (customItem.toppings.length > 0) {
        description += ' with ' + customItem.toppings.map(t => t.name).join(', ');
    }
    if (customItem.instructions) {
        description += `. Special instructions: ${customItem.instructions}`;
    }

    // Create custom item object
    const newCustomItem = {
        id: Date.now(), // Unique ID
        name: baseNames[customItem.base],
        description: description,
        price: itemPrice,
        category: customItem.base,
        image: 'https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
        isCustom: true
    };

    // Add to cart
    const existingItem = cart.find(cartItem =>
        cartItem.isCustom &&
        cartItem.name === newCustomItem.name &&
        cartItem.description === newCustomItem.description
    );

    if (existingItem) {
        existingItem.quantity += customItem.quantity;
    } else {
        cart.push({
            ...newCustomItem,
            quantity: customItem.quantity
        });
    }

    updateCartDisplay();
    updateCartPageDisplay();

    // Reset form
    document.getElementById('itemType').value = '';
    document.getElementById('quantity').value = '1';
    document.getElementById('specialInstructions').value = '';
    document.querySelectorAll('.topping-item input[type="checkbox"]').forEach(checkbox => {
        checkbox.checked = false;
    });

    customItem = {
        base: null,
        toppings: [],
        quantity: 1,
        instructions: ''
    };

    updateCustomPreview();

    // Show success message
    alert('Custom item added to cart!');

    // Optionally redirect to cart
    showSection('cart');
}

// Toggle cart sidebar
function toggleCart() {
    cartSidebar.classList.toggle('open');
    cartOverlay.classList.toggle('open');
}

// Scroll to menu section (legacy function for compatibility)
function scrollToMenu() {
    showSection('menu');
}

// Orders functionality
function loadOrderHistory() {
    // Load orders from localStorage
    const savedOrders = localStorage.getItem('foodieExpressOrders');
    if (savedOrders) {
        orders = JSON.parse(savedOrders);
    }
    updateOrdersDisplay();
}

function saveOrderHistory() {
    localStorage.setItem('foodieExpressOrders', JSON.stringify(orders));
}

function updateOrdersDisplay() {
    const currentOrdersContainer = document.getElementById('currentOrders');
    const orderHistoryContainer = document.getElementById('orderHistory');

    if (!currentOrdersContainer || !orderHistoryContainer) return;

    // Display current orders (orders placed in last 2 hours)
    const now = new Date();
    const twoHoursAgo = new Date(now.getTime() - 2 * 60 * 60 * 1000);

    const currentOrders = orders.filter(order =>
        new Date(order.date) > twoHoursAgo && order.status !== 'delivered'
    );

    const pastOrders = orders.filter(order =>
        new Date(order.date) <= twoHoursAgo || order.status === 'delivered'
    );

    // Display current orders
    if (currentOrders.length === 0) {
        currentOrdersContainer.innerHTML = '<p class="no-orders">No current orders</p>';
    } else {
        currentOrdersContainer.innerHTML = currentOrders.map(order => createOrderCard(order, true)).join('');
    }

    // Display order history
    if (pastOrders.length === 0) {
        orderHistoryContainer.innerHTML = '<p class="no-orders">No previous orders</p>';
    } else {
        orderHistoryContainer.innerHTML = pastOrders.slice(0, 10).map(order => createOrderCard(order, false)).join('');
    }
}

function createOrderCard(order, isCurrent) {
    const statusIcon = {
        'preparing': '👨‍🍳',
        'on-the-way': '🚗',
        'delivered': '✅'
    };

    const statusText = {
        'preparing': 'Preparing your order',
        'on-the-way': 'On the way',
        'delivered': 'Delivered'
    };

    return `
        <div class="order-card ${isCurrent ? 'current' : 'history'}">
            <div class="order-header">
                <div class="order-info">
                    <h4>Order #${order.id}</h4>
                    <p class="order-date">${new Date(order.date).toLocaleDateString()} at ${new Date(order.date).toLocaleTimeString()}</p>
                </div>
                <div class="order-status ${order.status}">
                    <span class="status-icon">${statusIcon[order.status]}</span>
                    <span class="status-text">${statusText[order.status]}</span>
                </div>
            </div>
            <div class="order-items">
                ${order.items.map(item => `
                    <div class="order-item">
                        <span>${item.quantity}x ${item.name}</span>
                        <span>$${(item.price * item.quantity).toFixed(2)}</span>
                    </div>
                `).join('')}
            </div>
            <div class="order-total">
                <strong>Total: $${order.total.toFixed(2)}</strong>
            </div>
        </div>
    `;
}

// Proceed to checkout from cart page
function proceedToCheckout() {
    if (cart.length === 0) {
        alert('Your cart is empty!');
        return;
    }

    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const deliveryFeeValue = getDeliveryFee();
    const tax = subtotal * 0.08;
    const total = subtotal + deliveryFeeValue + tax;
    const itemCount = cart.reduce((sum, item) => sum + item.quantity, 0);

    // Get delivery option
    const selectedDelivery = document.querySelector('input[name="delivery"]:checked');
    const deliveryType = selectedDelivery ? selectedDelivery.value : 'standard';
    const deliveryTime = deliveryType === 'express' ? '15-25 minutes' : '30-45 minutes';

    // Create order object
    const order = {
        id: Date.now().toString().slice(-6),
        date: new Date().toISOString(),
        items: [...cart],
        subtotal: subtotal,
        deliveryFee: deliveryFeeValue,
        tax: tax,
        total: total,
        deliveryType: deliveryType,
        status: 'preparing'
    };

    // Add to orders
    orders.unshift(order);
    saveOrderHistory();

    // Create detailed checkout message
    const orderSummary = cart.map(item =>
        `${item.quantity}x ${item.name} - $${(item.price * item.quantity).toFixed(2)}`
    ).join('\n');

    const message = `🎉 Order Confirmed!\n\n` +
                   `Order #${order.id}\n` +
                   `Order Summary:\n${orderSummary}\n\n` +
                   `Subtotal: $${subtotal.toFixed(2)}\n` +
                   `Delivery (${deliveryType}): $${deliveryFeeValue.toFixed(2)}\n` +
                   `Tax: $${tax.toFixed(2)}\n` +
                   `Total: $${total.toFixed(2)}\n\n` +
                   `📍 Estimated Delivery: ${deliveryTime}\n` +
                   `📱 You'll receive SMS updates about your order\n\n` +
                   `Thank you for choosing FoodieExpress!`;

    alert(message);

    // Clear cart
    cart = [];
    updateCartDisplay();
    updateCartPageDisplay();
    updateOrdersDisplay();

    // Show orders section
    showSection('orders');
}

// Checkout function (from sidebar)
function checkout() {
    if (cart.length === 0) {
        alert('Your cart is empty!');
        return;
    }

    // Redirect to cart page for full checkout experience
    showSection('cart');
    toggleCart();
}

// Update delivery fee when option changes
document.addEventListener('change', function(e) {
    if (e.target.name === 'delivery') {
        updateCartPageDisplay();
    }
});

// Smooth scrolling for navigation links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

// Close cart when clicking outside
document.addEventListener('click', function(e) {
    if (!cartSidebar.contains(e.target) && !e.target.closest('.cart-icon')) {
        if (cartSidebar.classList.contains('open')) {
            toggleCart();
        }
    }
});

// Prevent cart from closing when clicking inside
cartSidebar.addEventListener('click', function(e) {
    e.stopPropagation();
});
