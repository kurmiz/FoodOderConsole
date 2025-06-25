// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';
const CUSTOMER_ID = 'cli-customer-1'; // Use same customer ID as CLI for real integration

// Menu data (will be loaded from backend)
let menuItems = [];

// API Helper Functions
async function apiCall(endpoint, options = {}) {
    try {
        const url = `${API_BASE_URL}${endpoint}${endpoint.includes('?') ? '&' : '?'}customerId=${CUSTOMER_ID}`;
        const response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API call failed:', error);
        showNotification('Connection error. Using offline mode.', 'error');
        return null;
    }
}

// Load menu from backend
async function loadMenuFromBackend() {
    try {
        const data = await apiCall('/menu');
        if (data && Array.isArray(data)) {
            menuItems = data;
            console.log('✅ Menu loaded from backend:', menuItems.length, 'items');
            showNotification('Menu loaded from backend!', 'success');
            return true;
        }
    } catch (error) {
        console.error('Failed to load menu from backend:', error);
    }

    // Fallback to static menu if backend is not available
    menuItems = getStaticMenu();
    showNotification('Using offline menu data', 'warning');
    return false;
}

// Static menu fallback
function getStaticMenu() {
    return [
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
}

// Cart functionality - Real-time sync with backend
let cart = [];
let orders = [];
let currentSection = 'home';
let isBackendConnected = false;

// DOM elements
const menuGrid = document.getElementById('menuGrid');
const cartSidebar = document.getElementById('cartSidebar');
const cartOverlay = document.getElementById('cartOverlay');
const cartItems = document.getElementById('cartItems');
const cartCount = document.getElementById('cartCount');
const cartTotal = document.getElementById('cartTotal');

// Initialize the app
document.addEventListener('DOMContentLoaded', async function() {
    showNotification('🔄 Connecting to Java Backend...', 'info');

    // Check backend connection
    isBackendConnected = await checkBackendConnection();

    if (isBackendConnected) {
        // Load menu from backend
        await loadMenuFromBackend();

        // Load cart from backend
        await loadCartFromBackend();

        // Load orders from backend
        await loadOrdersFromBackend();

        showNotification('✅ Connected to Java Backend! Real-time sync active.', 'success');

        // Start real-time sync
        startRealTimeSync();

        // Add integration status indicator
        addIntegrationStatusIndicator();
    } else {
        // Use fallback data
        menuItems = getStaticMenu();
        showNotification('⚠️ Backend offline - using demo data', 'warning');
    }

    // Initialize UI
    displayMenuItems(menuItems);
    displayFeaturedItems();
    displayQuickAddItems();
    updateCartDisplay();
    updateCartPageDisplay();
    initializeAdminSection();
    showSection('home');

    // Setup admin form handler
    setupAdminFormHandlers();
});

// Add integration status indicator
function addIntegrationStatusIndicator() {
    const indicator = document.createElement('div');
    indicator.id = 'integrationStatus';
    indicator.className = 'integration-status';
    indicator.innerHTML = `
        <div class="integration-content">
            <span class="integration-icon">🔗</span>
            <div class="integration-text">
                <div class="integration-title">Java Backend Integration</div>
                <div class="integration-subtitle">Customer: ${CUSTOMER_ID}</div>
            </div>
            <div class="sync-indicator" id="syncIndicator">
                <span class="sync-dot"></span>
                <span class="sync-text">Synced</span>
            </div>
        </div>
    `;
    document.body.appendChild(indicator);
}

// Show sync activity
function showSyncActivity(message) {
    const syncIndicator = document.getElementById('syncIndicator');
    if (syncIndicator) {
        syncIndicator.innerHTML = `
            <span class="sync-dot syncing"></span>
            <span class="sync-text">${message}</span>
        `;

        setTimeout(() => {
            syncIndicator.innerHTML = `
                <span class="sync-dot"></span>
                <span class="sync-text">Synced</span>
            `;
        }, 2000);
    }
}

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
async function addToCart(itemId) {
    const item = menuItems.find(item => item.id === itemId);
    if (!item) return;

    if (isBackendConnected) {
        // Try to add to backend first
        const success = await addToCartBackend(itemId, 1, '');

        if (success) {
            // Immediately sync cart from backend to get real data
            await loadCartFromBackend();
            updateCartDisplay();
            updateCartPageDisplay();

            showNotification(`${item.name} added to cart! (Backend)`, 'success');
        } else {
            showNotification('Failed to add item to cart', 'error');
            return;
        }
    } else {
        // Fallback to local cart if backend is offline
        const existingItem = cart.find(cartItem => cartItem.id === itemId);

        if (existingItem) {
            existingItem.quantity += 1;
        } else {
            cart.push({
                ...item,
                quantity: 1,
                instructions: ''
            });
        }

        updateCartDisplay();
        updateCartPageDisplay();
        showNotification(`${item.name} added to cart! (Offline)`, 'warning');
    }

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

// Backend cart operations
async function addToCartBackend(itemId, quantity, instructions = '') {
    const data = await apiCall('/cart', {
        method: 'POST',
        body: JSON.stringify({
            itemId: itemId,
            quantity: quantity,
            instructions: instructions
        })
    });

    return data && data.success;
}

async function loadCartFromBackend() {
    try {
        const data = await apiCall('/cart');
        if (data && data.items) {
            // Convert backend cart format to frontend format
            cart = data.items.map(item => ({
                id: item.id,
                name: item.name,
                description: item.description,
                price: item.price,
                category: item.category,
                image: item.image,
                quantity: item.quantity,
                instructions: item.instructions || ''
            }));
            console.log('✅ Cart loaded from backend:', cart.length, 'items');
            return true;
        }
    } catch (error) {
        console.error('Failed to load cart from backend:', error);
    }
    return false;
}

// Real-time synchronization
function startRealTimeSync() {
    // Sync cart every 5 seconds
    setInterval(async () => {
        if (isBackendConnected) {
            await syncCartWithBackend();
        }
    }, 5000);

    // Sync orders every 10 seconds
    setInterval(async () => {
        if (isBackendConnected) {
            await syncOrdersWithBackend();
        }
    }, 10000);
}

async function syncCartWithBackend() {
    try {
        showSyncActivity('Syncing cart...');
        const data = await apiCall('/cart');
        if (data && data.items) {
            const backendCart = data.items.map(item => ({
                id: item.id,
                name: item.name,
                description: item.description,
                price: item.price,
                category: item.category,
                image: item.image,
                quantity: item.quantity,
                instructions: item.instructions || ''
            }));

            // Only update if cart has changed
            if (JSON.stringify(cart) !== JSON.stringify(backendCart)) {
                cart = backendCart;
                updateCartDisplay();
                updateCartPageDisplay();
                console.log('🔄 Cart synced with backend - Items:', cart.length);
                showNotification(`Cart synced: ${cart.length} items`, 'info');
            }
        }
    } catch (error) {
        console.error('Cart sync failed:', error);
        isBackendConnected = false;
        updateConnectionStatus(false);
    }
}

async function syncOrdersWithBackend() {
    try {
        const data = await apiCall('/orders');
        if (data && Array.isArray(data)) {
            const backendOrders = data.map(order => ({
                id: order.orderId,
                date: order.orderTime,
                items: [],
                total: order.totalAmount,
                status: order.status.toLowerCase().replace('_', '-')
            }));

            // Only update if orders have changed
            if (JSON.stringify(orders) !== JSON.stringify(backendOrders)) {
                orders = backendOrders;
                updateOrdersDisplay();
                console.log('🔄 Orders synced with backend');
            }
        }
    } catch (error) {
        console.error('Orders sync failed:', error);
    }
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
async function removeFromCart(itemId) {
    if (isBackendConnected) {
        const success = await removeFromCartBackend(itemId);
        if (success) {
            // Reload cart from backend
            await loadCartFromBackend();
            updateCartDisplay();
            updateCartPageDisplay();
            showNotification('Item removed from cart!', 'success');
        } else {
            showNotification('Failed to remove item', 'error');
        }
    } else {
        // Fallback to local cart
        cart = cart.filter(item => item.id !== itemId);
        updateCartDisplay();
        updateCartPageDisplay();
        showNotification('Item removed from cart! (Offline)', 'warning');
    }
}

// Update item quantity in cart
async function updateQuantity(itemId, change) {
    if (isBackendConnected) {
        // Get current item from cart
        const item = cart.find(cartItem => cartItem.id === itemId);
        if (!item) return;

        const newQuantity = item.quantity + change;

        if (newQuantity <= 0) {
            // Remove item
            await removeFromCartBackend(itemId);
        } else {
            // Update quantity
            await updateCartItemBackend(itemId, newQuantity);
        }

        // Reload cart from backend
        await loadCartFromBackend();
        updateCartDisplay();
        updateCartPageDisplay();
    } else {
        // Fallback to local cart
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
}

// Backend cart update functions
async function updateCartItemBackend(itemId, quantity) {
    const data = await apiCall('/cart', {
        method: 'PUT',
        body: JSON.stringify({
            itemId: itemId,
            quantity: quantity
        })
    });
    return data && data.success;
}

async function removeFromCartBackend(itemId) {
    const data = await apiCall(`/cart?itemId=${itemId}`, {
        method: 'DELETE'
    });
    return data && data.success;
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
async function proceedToCheckout() {
    if (cart.length === 0) {
        showNotification('Your cart is empty!', 'error');
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

    if (isBackendConnected) {
        // Create order via backend
        const orderData = await createOrderBackend(deliveryType, '123 Web Street, Online City', '555-WEB-USER');

        if (orderData && orderData.success) {
            // Create detailed checkout message
            const orderSummary = cart.map(item =>
                `${item.quantity}x ${item.name} - $${(item.price * item.quantity).toFixed(2)}`
            ).join('\n');

            const message = `🎉 Order Confirmed! (Backend)\n\n` +
                           `Order #${orderData.orderId}\n` +
                           `Order Summary:\n${orderSummary}\n\n` +
                           `Subtotal: $${subtotal.toFixed(2)}\n` +
                           `Delivery (${deliveryType}): $${deliveryFeeValue.toFixed(2)}\n` +
                           `Tax: $${tax.toFixed(2)}\n` +
                           `Total: $${orderData.total.toFixed(2)}\n\n` +
                           `📍 Estimated Delivery: ${deliveryTime}\n` +
                           `📱 You'll receive SMS updates about your order\n\n` +
                           `✅ Order saved to Java Backend!\n` +
                           `Thank you for choosing FoodieExpress!`;

            alert(message);

            // Reload cart and orders from backend to get real-time data
            await loadCartFromBackend();
            await loadOrdersFromBackend();

            updateCartDisplay();
            updateCartPageDisplay();
            updateOrdersDisplay();

            // Show orders section
            showSection('orders');

            showNotification(`Order #${orderData.orderId} placed successfully! (Backend)`, 'success');
        } else {
            showNotification('Failed to place order. Please try again.', 'error');
        }
    } else {
        // Fallback to local order creation
        const order = {
            id: Date.now().toString().slice(-6),
            date: new Date().toISOString(),
            items: [...cart],
            total: total,
            status: 'preparing'
        };

        orders.unshift(order);
        cart = [];

        updateCartDisplay();
        updateCartPageDisplay();
        updateOrdersDisplay();

        showNotification(`Order #${order.id} placed! (Offline Mode)`, 'warning');
        showSection('orders');
    }
}

// Backend order operations
async function createOrderBackend(deliveryType, address, phone) {
    const data = await apiCall('/orders', {
        method: 'POST',
        body: JSON.stringify({
            deliveryType: deliveryType,
            address: address,
            phone: phone
        })
    });

    return data;
}

async function loadOrdersFromBackend() {
    try {
        const data = await apiCall('/orders');
        if (data && Array.isArray(data)) {
            // Convert backend orders to frontend format
            orders = data.map(order => ({
                id: order.orderId,
                date: order.orderTime,
                items: [], // Items not needed for display
                total: order.totalAmount,
                status: order.status.toLowerCase().replace('_', '-')
            }));
            console.log('✅ Orders loaded from backend:', orders.length, 'orders');
            return true;
        }
    } catch (error) {
        console.error('Failed to load orders from backend:', error);
    }
    return false;
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

// Notification system
function showNotification(message, type = 'info') {
    // Remove existing notifications
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notification => notification.remove());

    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <span class="notification-icon">${getNotificationIcon(type)}</span>
            <span class="notification-message">${message}</span>
            <button class="notification-close" onclick="this.parentElement.parentElement.remove()">×</button>
        </div>
    `;

    // Add to page
    document.body.appendChild(notification);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentElement) {
            notification.remove();
        }
    }, 5000);

    // Add animation
    setTimeout(() => {
        notification.classList.add('show');
    }, 100);
}

function getNotificationIcon(type) {
    switch (type) {
        case 'success': return '✅';
        case 'error': return '❌';
        case 'warning': return '⚠️';
        case 'info': return 'ℹ️';
        default: return 'ℹ️';
    }
}

// Connection status indicator
function updateConnectionStatus(isConnected) {
    let statusIndicator = document.getElementById('connectionStatus');

    if (!statusIndicator) {
        statusIndicator = document.createElement('div');
        statusIndicator.id = 'connectionStatus';
        statusIndicator.className = 'connection-status';
        document.body.appendChild(statusIndicator);
    }

    statusIndicator.innerHTML = `
        <span class="status-dot ${isConnected ? 'connected' : 'disconnected'}"></span>
        <span class="status-text">${isConnected ? 'Connected to Backend' : 'Offline Mode'}</span>
    `;

    statusIndicator.className = `connection-status ${isConnected ? 'connected' : 'disconnected'}`;
}

// Check backend connection
async function checkBackendConnection() {
    try {
        const response = await fetch(`${API_BASE_URL}/menu`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const wasConnected = isBackendConnected;
        isBackendConnected = response.ok;

        updateConnectionStatus(isBackendConnected);

        // Show notification on connection status change
        if (!wasConnected && isBackendConnected) {
            showNotification('✅ Connected to Java Backend!', 'success');
        } else if (wasConnected && !isBackendConnected) {
            showNotification('❌ Lost connection to backend', 'error');
        }

        return isBackendConnected;
    } catch (error) {
        const wasConnected = isBackendConnected;
        isBackendConnected = false;
        updateConnectionStatus(false);

        if (wasConnected) {
            showNotification('❌ Backend connection lost', 'error');
        }

        return false;
    }
}

// Periodically check connection and sync data
setInterval(async () => {
    await checkBackendConnection();

    // If connected, sync data
    if (isBackendConnected) {
        await syncCartWithBackend();
        await syncOrdersWithBackend();
        await syncMenuWithBackend();
    }
}, 10000); // Check every 10 seconds

// Admin Section Functions
function initializeAdminSection() {
    displayAdminMenuItems();
    updateMenuStatistics();
}

function setupAdminFormHandlers() {
    const addMenuForm = document.getElementById('addMenuItemForm');
    if (addMenuForm) {
        addMenuForm.addEventListener('submit', handleAddMenuItem);
    }
}

async function handleAddMenuItem(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const menuItemData = {
        name: formData.get('name'),
        description: formData.get('description'),
        price: parseFloat(formData.get('price')),
        category: formData.get('category'),
        imageUrl: formData.get('imageUrl') || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c'
    };

    if (isBackendConnected) {
        const success = await addMenuItemToBackend(menuItemData);
        if (success) {
            showNotification('✅ Menu item added successfully!', 'success');
            event.target.reset();

            // Reload menu and update displays
            await loadMenuFromBackend();
            displayMenuItems(menuItems);
            displayFeaturedItems();
            displayAdminMenuItems();
            updateMenuStatistics();
        } else {
            showNotification('❌ Failed to add menu item', 'error');
        }
    } else {
        // Fallback to local addition
        const newItem = {
            id: Math.max(...menuItems.map(item => item.id)) + 1,
            ...menuItemData,
            available: true
        };

        menuItems.push(newItem);
        displayMenuItems(menuItems);
        displayFeaturedItems();
        displayAdminMenuItems();
        updateMenuStatistics();

        showNotification('✅ Menu item added locally!', 'warning');
        event.target.reset();
    }
}

async function addMenuItemToBackend(itemData) {
    const data = await apiCall('/menu', {
        method: 'POST',
        body: JSON.stringify(itemData)
    });
    return data && data.success;
}

async function deleteMenuItemFromBackend(itemId) {
    const data = await apiCall(`/menu?itemId=${itemId}`, {
        method: 'DELETE'
    });
    return data && data.success;
}

async function updateMenuItemInBackend(itemId, itemData) {
    const data = await apiCall(`/menu?itemId=${itemId}`, {
        method: 'PUT',
        body: JSON.stringify(itemData)
    });
    return data && data.success;
}

function displayAdminMenuItems() {
    const adminMenuList = document.getElementById('adminMenuList');
    if (!adminMenuList) return;

    adminMenuList.innerHTML = menuItems.map(item => `
        <div class="admin-menu-item" data-id="${item.id}">
            <img src="${item.image || item.imageUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c'}"
                 alt="${item.name}" class="admin-item-image"
                 onerror="this.src='https://images.unsplash.com/photo-1546069901-ba9599a7e63c'">
            <div class="admin-item-info">
                <div class="admin-item-name">${item.name}</div>
                <div class="admin-item-details">
                    <span class="admin-item-price">$${item.price.toFixed(2)}</span>
                    <span class="admin-item-category">${item.category}</span>
                    <span class="admin-item-status ${item.available ? 'available' : 'unavailable'}">
                        ${item.available ? '✅ Available' : '❌ Unavailable'}
                    </span>
                </div>
                <div class="admin-item-description">${item.description.substring(0, 100)}...</div>
            </div>
            <div class="admin-item-actions">
                <button class="btn-edit" onclick="editMenuItem(${item.id})" title="Edit Item">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn-delete" onclick="deleteMenuItem(${item.id})" title="Delete Item">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `).join('');
}

async function deleteMenuItem(itemId) {
    if (!confirm('Are you sure you want to delete this menu item?')) {
        return;
    }

    if (isBackendConnected) {
        const success = await deleteMenuItemFromBackend(itemId);
        if (success) {
            showNotification('✅ Menu item deleted successfully!', 'success');

            // Reload menu and update displays
            await loadMenuFromBackend();
            displayMenuItems(menuItems);
            displayFeaturedItems();
            displayAdminMenuItems();
            updateMenuStatistics();
        } else {
            showNotification('❌ Failed to delete menu item', 'error');
        }
    } else {
        // Fallback to local deletion
        menuItems = menuItems.filter(item => item.id !== itemId);
        displayMenuItems(menuItems);
        displayFeaturedItems();
        displayAdminMenuItems();
        updateMenuStatistics();

        showNotification('✅ Menu item deleted locally!', 'warning');
    }
}

function editMenuItem(itemId) {
    const item = menuItems.find(item => item.id === itemId);
    if (!item) return;

    // Fill the form with current item data
    document.getElementById('itemName').value = item.name;
    document.getElementById('itemDescription').value = item.description;
    document.getElementById('itemPrice').value = item.price;
    document.getElementById('itemCategory').value = item.category;
    document.getElementById('itemImage').value = item.image || item.imageUrl || '';

    // Change form to edit mode
    const form = document.getElementById('addMenuItemForm');
    const submitButton = form.querySelector('button[type="submit"]');

    submitButton.innerHTML = '<i class="fas fa-save"></i> Update Menu Item';
    submitButton.onclick = (e) => handleUpdateMenuItem(e, itemId);

    // Scroll to form
    form.scrollIntoView({ behavior: 'smooth' });

    showNotification('📝 Edit mode activated', 'info');
}

async function handleUpdateMenuItem(event, itemId) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const menuItemData = {
        name: formData.get('name'),
        description: formData.get('description'),
        price: parseFloat(formData.get('price')),
        category: formData.get('category'),
        imageUrl: formData.get('imageUrl') || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c'
    };

    if (isBackendConnected) {
        const success = await updateMenuItemInBackend(itemId, menuItemData);
        if (success) {
            showNotification('✅ Menu item updated successfully!', 'success');
            resetAdminForm();

            // Reload menu and update displays
            await loadMenuFromBackend();
            displayMenuItems(menuItems);
            displayFeaturedItems();
            displayAdminMenuItems();
            updateMenuStatistics();
        } else {
            showNotification('❌ Failed to update menu item', 'error');
        }
    } else {
        // Fallback to local update
        const itemIndex = menuItems.findIndex(item => item.id === itemId);
        if (itemIndex !== -1) {
            menuItems[itemIndex] = { ...menuItems[itemIndex], ...menuItemData };
            displayMenuItems(menuItems);
            displayFeaturedItems();
            displayAdminMenuItems();
            updateMenuStatistics();

            showNotification('✅ Menu item updated locally!', 'warning');
            resetAdminForm();
        }
    }
}

function resetAdminForm() {
    const form = document.getElementById('addMenuItemForm');
    const submitButton = form.querySelector('button[type="submit"]');

    form.reset();
    submitButton.innerHTML = '<i class="fas fa-plus"></i> Add Menu Item';
    submitButton.onclick = null;
}

function updateMenuStatistics() {
    const totalItems = menuItems.length;
    const categories = [...new Set(menuItems.map(item => item.category))].length;
    const avgPrice = menuItems.reduce((sum, item) => sum + item.price, 0) / totalItems;

    document.getElementById('totalItems').textContent = totalItems;
    document.getElementById('totalCategories').textContent = categories;
    document.getElementById('avgPrice').textContent = `$${avgPrice.toFixed(2)}`;
}

async function syncMenuWithBackend() {
    try {
        const data = await apiCall('/menu');
        if (data && Array.isArray(data)) {
            const backendMenu = data;

            // Only update if menu has changed
            if (JSON.stringify(menuItems) !== JSON.stringify(backendMenu)) {
                menuItems = backendMenu;
                displayMenuItems(menuItems);
                displayFeaturedItems();
                displayAdminMenuItems();
                updateMenuStatistics();
                console.log('🔄 Menu synced with backend - Items:', menuItems.length);
            }
        }
    } catch (error) {
        console.error('Menu sync failed:', error);
    }
}
