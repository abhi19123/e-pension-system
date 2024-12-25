// Handle JWT token storage and retrieval
const AUTH_TOKEN_KEY = 'auth_token';
const USER_KEY = 'user_data';

function saveAuthToken(token) {
    localStorage.setItem(AUTH_TOKEN_KEY, token);
}

function getAuthToken() {
    return localStorage.getItem(AUTH_TOKEN_KEY);
}

function clearAuthToken() {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

function saveUserData(userData) {
    localStorage.setItem(USER_KEY, JSON.stringify(userData));
}

function getUserData() {
    const data = localStorage.getItem(USER_KEY);
    return data ? JSON.parse(data) : null;
}

// Add Authorization header to all requests
function addAuthHeader(headers = {}) {
    const token = getAuthToken();
    if (token) {
        return {
            ...headers,
            'Authorization': `Bearer ${token}`
        };
    }
    return headers;
}

// Handle login
async function handleLogin(phoneNumber, password, role) {
    try {
        console.log('Attempting login with:', { phoneNumber, role });
        
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                phoneNumber: phoneNumber.startsWith('+91') ? phoneNumber : '+91' + phoneNumber,
                password,
                role
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Login failed:', errorText);
            throw new Error(errorText || 'Login failed');
        }

        const data = await response.json();
        console.log('Login response:', data);
        
        if (data.token) {
            saveAuthToken(data.token);
            saveUserData(data.user || { phoneNumber, role });
            
            // Redirect based on role
            const dashboardUrl = role.toLowerCase() === 'admin' ? '/admin/dashboard' : '/pensioner/dashboard';
            console.log('Redirecting to:', dashboardUrl);
            window.location.href = dashboardUrl;
        } else {
            throw new Error(data.message || 'Login failed - No token received');
        }
    } catch (error) {
        console.error('Login error:', error);
        throw error;
    }
}

// Handle logout
function handleLogout() {
    clearAuthToken();
    window.location.href = '/login';
}

// Check if user is authenticated
function isAuthenticated() {
    return !!getAuthToken();
}

// Protect routes
function protectRoute() {
    if (!isAuthenticated()) {
        window.location.href = '/login?unauthorized=true';
    }
}

// Intercept all fetch requests to add auth header
const originalFetch = window.fetch;
window.fetch = async function() {
    let [resource, config] = arguments;
    
    // Skip auth header for login and register endpoints
    if (typeof resource === 'string' && 
        (resource.includes('/api/auth/login') || 
         resource.includes('/api/auth/register'))) {
        return originalFetch.apply(this, arguments);
    }

    // Add auth header to all other requests
    config = config || {};
    config.headers = addAuthHeader(config.headers || {});
    
    try {
        const response = await originalFetch(resource, config);
        
        // Handle 401 Unauthorized responses
        if (response.status === 401) {
            clearAuthToken();
            window.location.href = '/login?unauthorized=true';
            return Promise.reject('Unauthorized');
        }
        
        return response;
    } catch (error) {
        console.error('Fetch error:', error);
        throw error;
    }
};

// Initialize protection on protected routes
if (window.location.pathname.includes('/admin/') || 
    window.location.pathname.includes('/pensioner/')) {
    protectRoute();
}

// Export functions for use in other files
window.auth = {
    handleLogin,
    handleLogout,
    isAuthenticated,
    protectRoute,
    getAuthToken,
    getUserData
};
