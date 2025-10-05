# Frontend Application Integration Guide

This guide explains how to integrate a frontend application (React, Vue, Angular, etc.) with the Spring Boot microservices backend using Auth0 OAuth2 authentication.

## ⚠️ Prerequisites: Auth0 Application Configuration

**IMPORTANT:** Your CloudGateway must use a **Regular Web Application** in Auth0 (NOT Machine-to-Machine).

### Why?

Frontend user login requires **Authorization Code flow**, which is only supported by **Regular Web Applications**.

**Machine-to-Machine (M2M)** applications only support **Client Credentials flow** (service-to-service calls) and **cannot** handle browser-based user login.

### Configuration Required

1. **Create/Use Regular Web Application in Auth0:**
   - Type: Regular Web Application
   - Grant Types: ✅ Authorization Code, ✅ Refresh Token
   - Allowed Callback URLs: `http://localhost:9090/login/oauth2/code/auth0`
   - Allowed Logout URLs: `http://localhost:9090`
   - Allowed Web Origins: `http://localhost:3000,http://localhost:4200,http://localhost:5173`

2. **Update `.env` file:**
   ```bash
   AUTH0_CLIENT_ID=<your_regular_web_app_client_id>
   AUTH0_CLIENT_SECRET=<your_regular_web_app_client_secret>
   ```

3. **Optional:** If using separate M2M app for backend services:
   ```bash
   AUTH0_M2M_CLIENT_ID=<your_m2m_client_id>
   AUTH0_M2M_CLIENT_SECRET=<your_m2m_client_secret>
   ```

For detailed instructions, see [README.md - Troubleshooting #8](#do-i-need-to-change-my-m2m-app)

---

## Table of Contents
- [Authentication Flow Options](#authentication-flow-options)
- [Option 1: Session-Based (Recommended for Web Apps)](#option-1-session-based-recommended-for-web-apps)
- [Option 2: Token-Based (SPA with Backend-for-Frontend)](#option-2-token-based-spa-with-backend-for-frontend)
- [Option 3: Direct Auth0 SDK (Advanced)](#option-3-direct-auth0-sdk-advanced)
- [Configuration](#configuration)
- [Complete Examples](#complete-examples)

---

## Authentication Flow Options

### **Option 1: Session-Based** ✅ Recommended
- **Best for:** Traditional web apps, server-side rendered apps
- **How:** OAuth2 Authorization Code flow with session cookies
- **Pros:** Simple, secure (cookies are HTTP-only)
- **Cons:** Requires same-domain or CORS setup

### **Option 2: Token-Based with BFF** ✅ Recommended for SPAs
- **Best for:** Single Page Applications (React, Vue, Angular)
- **How:** Backend-for-Frontend pattern with token exchange
- **Pros:** Better security than storing tokens in localStorage
- **Cons:** Requires additional endpoint

### **Option 3: Direct Auth0 SDK** ⚠️ Advanced
- **Best for:** Mobile apps, native apps
- **How:** Frontend directly integrates with Auth0
- **Pros:** Full control, works for mobile
- **Cons:** Tokens stored in frontend (less secure)

---

## Option 1: Session-Based (Recommended for Web Apps)

### **How It Works**

```
┌─────────────┐
│   Browser   │
│  (Frontend) │
└──────┬──────┘
       │
       │ 1. Click "Login" → Redirect to /oauth2/authorization/auth0
       │
       ▼
┌─────────────────┐
│ Cloud Gateway   │
│ (localhost:9090)│
└────────┬────────┘
         │
         │ 2. Redirect to Auth0 login page
         │
         ▼
┌─────────────────┐
│  Auth0 Login    │
│  (auth0.com)    │
└────────┬────────┘
         │
         │ 3. User logs in
         │ 4. Redirect back with code
         │
         ▼
┌─────────────────┐
│ Cloud Gateway   │
└────────┬────────┘
         │
         │ 5. Exchange code for tokens
         │ 6. Create session cookie
         │ 7. Redirect to frontend
         │
         ▼
┌─────────────┐
│   Browser   │
│ (Logged in) │
└─────────────┘
```

### **Frontend Implementation (React)**

#### **1. Login Component**

```jsx
// src/components/Login.jsx
import React from 'react';

function Login() {
  const handleLogin = () => {
    // Redirect to Cloud Gateway OAuth2 endpoint
    window.location.href = 'http://localhost:9090/oauth2/authorization/auth0';
  };

  return (
    <div className="login-page">
      <h1>Welcome to E-Commerce App</h1>
      <button onClick={handleLogin} className="btn-login">
        Login with Auth0
      </button>
    </div>
  );
}

export default Login;
```

#### **2. Protected API Calls**

```jsx
// src/services/api.js
const API_BASE_URL = 'http://localhost:9090';

// Fetch wrapper with credentials
async function apiCall(endpoint, options = {}) {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    credentials: 'include', // IMPORTANT: Sends cookies
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  if (!response.ok) {
    if (response.status === 401) {
      // Redirect to login if unauthorized
      window.location.href = '/login';
    }
    throw new Error(`API Error: ${response.status}`);
  }

  return response.json();
}

// Products API
export const getProducts = () => apiCall('/product');
export const getProduct = (id) => apiCall(`/product/${id}`);
export const createProduct = (product) =>
  apiCall('/product', { method: 'POST', body: JSON.stringify(product) });

// Orders API
export const getOrders = () => apiCall('/order');
export const placeOrder = (order) =>
  apiCall('/order/placeOrder', { method: 'POST', body: JSON.stringify(order) });

// User Info
export const getUserInfo = () => apiCall('/authenticate/login');
```

#### **3. User Profile Component**

```jsx
// src/components/UserProfile.jsx
import React, { useEffect, useState } from 'react';
import { getUserInfo } from '../services/api';

function UserProfile() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getUserInfo()
      .then(data => {
        setUser(data);
        setLoading(false);
      })
      .catch(error => {
        console.error('Failed to load user:', error);
        setLoading(false);
      });
  }, []);

  if (loading) return <div>Loading...</div>;
  if (!user) return null;

  return (
    <div className="user-profile">
      <h3>Welcome, {user.userId}</h3>
      <p>Token expires: {new Date(user.expiresAt * 1000).toLocaleString()}</p>
      <details>
        <summary>User Details</summary>
        <pre>{JSON.stringify(user, null, 2)}</pre>
      </details>
    </div>
  );
}

export default UserProfile;
```

#### **4. Product List Component**

```jsx
// src/components/ProductList.jsx
import React, { useEffect, useState } from 'react';
import { getProducts } from '../services/api';

function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getProducts()
      .then(data => {
        setProducts(data);
        setLoading(false);
      })
      .catch(error => {
        console.error('Failed to load products:', error);
        setLoading(false);
      });
  }, []);

  if (loading) return <div>Loading products...</div>;

  return (
    <div className="product-list">
      <h2>Products</h2>
      <div className="products-grid">
        {products.map(product => (
          <div key={product.id} className="product-card">
            <h3>{product.name}</h3>
            <p>Price: ${product.price}</p>
            <p>Stock: {product.quantity}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

export default ProductList;
```

#### **5. App Router**

```jsx
// src/App.jsx
import React, { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import ProductList from './components/ProductList';
import UserProfile from './components/UserProfile';
import { getUserInfo } from './services/api';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is authenticated
    getUserInfo()
      .then(() => {
        setIsAuthenticated(true);
        setLoading(false);
      })
      .catch(() => {
        setIsAuthenticated(false);
        setLoading(false);
      });
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/"
          element={
            isAuthenticated ? (
              <div>
                <UserProfile />
                <ProductList />
              </div>
            ) : (
              <Navigate to="/login" />
            )
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

---

## Option 2: Token-Based (SPA with Backend-for-Frontend)

For SPAs that need to store tokens and make direct API calls.

### **Backend: Add Token Endpoint**

Create a new endpoint in CloudGateway to return just the access token:

```java
// CloudGateway/src/main/java/com/ebelemgnegre/CloudGateway/controller/AuthenticationController.java

@GetMapping("/token")
public ResponseEntity<Map<String, String>> getToken(
        @RegisteredOAuth2AuthorizedClient("auth0") OAuth2AuthorizedClient client) {

    String accessToken = client.getAccessToken().getTokenValue();
    long expiresAt = client.getAccessToken().getExpiresAt().getEpochSecond();

    Map<String, String> response = new HashMap<>();
    response.put("access_token", accessToken);
    response.put("expires_at", String.valueOf(expiresAt));

    return ResponseEntity.ok(response);
}
```

### **Frontend Implementation**

```jsx
// src/services/auth.js
const API_BASE_URL = 'http://localhost:9090';

export const login = () => {
  // Step 1: Redirect to OAuth2
  window.location.href = `${API_BASE_URL}/oauth2/authorization/auth0`;
};

export const getToken = async () => {
  // Step 2: After redirect, get token
  const response = await fetch(`${API_BASE_URL}/authenticate/token`, {
    credentials: 'include',
  });

  if (!response.ok) throw new Error('Not authenticated');

  const data = await response.json();

  // Store token (consider using sessionStorage instead of localStorage for better security)
  sessionStorage.setItem('access_token', data.access_token);
  sessionStorage.setItem('expires_at', data.expires_at);

  return data.access_token;
};

export const getStoredToken = () => {
  return sessionStorage.getItem('access_token');
};

export const isTokenExpired = () => {
  const expiresAt = sessionStorage.getItem('expires_at');
  if (!expiresAt) return true;

  return Date.now() / 1000 > parseInt(expiresAt);
};

export const logout = () => {
  sessionStorage.removeItem('access_token');
  sessionStorage.removeItem('expires_at');
  window.location.href = '/login';
};
```

```jsx
// src/services/api.js
import { getStoredToken, isTokenExpired, logout } from './auth';

async function apiCallWithToken(endpoint, options = {}) {
  if (isTokenExpired()) {
    logout();
    return;
  }

  const token = getStoredToken();

  const response = await fetch(`http://localhost:9090${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      ...options.headers,
    },
  });

  if (!response.ok) {
    if (response.status === 401) {
      logout();
    }
    throw new Error(`API Error: ${response.status}`);
  }

  return response.json();
}

export const getProducts = () => apiCallWithToken('/product');
```

---

## Option 3: Direct Auth0 SDK (Advanced)

For maximum flexibility, integrate Auth0 SDK directly in frontend.

### **Installation**

```bash
npm install @auth0/auth0-react
```

### **Setup**

```jsx
// src/index.js
import { Auth0Provider } from '@auth0/auth0-react';

ReactDOM.render(
  <Auth0Provider
    domain="dev-5nw6367bfpr277ec.us.auth0.com"
    clientId="YOUR_AUTH0_CLIENT_ID"
    authorizationParams={{
      redirect_uri: window.location.origin,
      audience: "http://springboot-microservices-api"
    }}
  >
    <App />
  </Auth0Provider>,
  document.getElementById('root')
);
```

### **Usage**

```jsx
// src/components/Login.jsx
import { useAuth0 } from '@auth0/auth0-react';

function Login() {
  const { loginWithRedirect } = useAuth0();

  return (
    <button onClick={() => loginWithRedirect()}>
      Login with Auth0
    </button>
  );
}
```

```jsx
// src/services/api.js
import { useAuth0 } from '@auth0/auth0-react';

function useApiCall() {
  const { getAccessTokenSilently } = useAuth0();

  const apiCall = async (endpoint, options = {}) => {
    const token = await getAccessTokenSilently();

    const response = await fetch(`http://localhost:9090${endpoint}`, {
      ...options,
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
        ...options.headers,
      },
    });

    return response.json();
  };

  return apiCall;
}

// Usage in component
function ProductList() {
  const [products, setProducts] = useState([]);
  const apiCall = useApiCall();

  useEffect(() => {
    apiCall('/product').then(setProducts);
  }, []);

  // ...
}
```

---

## Configuration

### **1. Update Auth0 Application Settings**

Go to: https://manage.auth0.com/dashboard/us/dev-5nw6367bfpr277ec/applications

Add your frontend URLs to:

**Allowed Callback URLs:**
```
http://localhost:3000,
http://localhost:9090/login/oauth2/code/auth0
```

**Allowed Logout URLs:**
```
http://localhost:3000,
http://localhost:9090
```

**Allowed Web Origins:**
```
http://localhost:3000,
http://localhost:9090
```

**Allowed Origins (CORS):**
```
http://localhost:3000
```

### **2. Update CloudGateway CORS**

Already configured in `CorsConfig.java` to allow:
- `http://localhost:3000` (React)
- `http://localhost:4200` (Angular)
- `http://localhost:8080` (Vue)
- `http://localhost:5173` (Vite)

### **3. Restart Services**

```bash
docker-compose -f docker-compose.dev.yml restart cloudgateway
```

---

## Complete Examples

### **React Example (Create React App)**

```bash
# Create app
npx create-react-app ecommerce-frontend
cd ecommerce-frontend

# Install dependencies
npm install react-router-dom

# Copy the code examples above to:
# - src/services/api.js
# - src/components/Login.jsx
# - src/components/UserProfile.jsx
# - src/components/ProductList.jsx
# - src/App.jsx

# Start
npm start
```

Visit: http://localhost:3000

### **Vue Example**

```bash
# Create app
npm create vue@latest ecommerce-frontend
cd ecommerce-frontend
npm install

# Create files
touch src/services/api.js
```

```js
// src/services/api.js
export const login = () => {
  window.location.href = 'http://localhost:9090/oauth2/authorization/auth0';
};

export const apiCall = async (endpoint, options = {}) => {
  const response = await fetch(`http://localhost:9090${endpoint}`, {
    ...options,
    credentials: 'include',
  });
  return response.json();
};
```

```vue
<!-- src/views/Login.vue -->
<template>
  <div class="login">
    <h1>Login</h1>
    <button @click="handleLogin">Login with Auth0</button>
  </div>
</template>

<script>
import { login } from '@/services/api';

export default {
  methods: {
    handleLogin() {
      login();
    }
  }
}
</script>
```

---

## Testing

### **1. Test Login Flow**

1. Start backend: `docker-compose -f docker-compose.dev.yml up -d`
2. Start frontend: `npm start`
3. Navigate to: `http://localhost:3000`
4. Click "Login"
5. Should redirect to Auth0
6. After login, redirects back to frontend

### **2. Test API Calls**

```javascript
// In browser console
fetch('http://localhost:9090/product', {
  credentials: 'include'
})
  .then(r => r.json())
  .then(console.log);
```

### **3. Test Token**

```javascript
// Get user info
fetch('http://localhost:9090/authenticate/login', {
  credentials: 'include'
})
  .then(r => r.json())
  .then(data => {
    console.log('User:', data.userId);
    console.log('Token:', data.accessToken);
  });
```

---

## Troubleshooting

### **CORS Issues**

**Problem:** Browser shows CORS error

**Solution:**
1. Verify `CorsConfig.java` includes your frontend URL
2. Restart CloudGateway: `docker-compose -f docker-compose.dev.yml restart cloudgateway`
3. Check browser console for specific CORS error

### **401 Unauthorized**

**Problem:** API calls return 401

**Solutions:**
1. Check cookies are being sent: Use `credentials: 'include'`
2. Verify you're logged in: Visit `http://localhost:9090/authenticate/login` directly
3. Check Auth0 session: Clear cookies and log in again

### **Redirect Loop**

**Problem:** Keeps redirecting between Auth0 and app

**Solutions:**
1. Check Auth0 Allowed Callback URLs include: `http://localhost:9090/login/oauth2/code/auth0`
2. Verify `AUTH0_CLIENT_ID` and `AUTH0_CLIENT_SECRET` are correct
3. Check browser cookies are enabled

---

## Security Best Practices

✅ **DO:**
- Use `credentials: 'include'` for session cookies
- Use `sessionStorage` instead of `localStorage` for tokens
- Implement token expiry checks
- Use HTTPS in production
- Set secure, HTTP-only cookies in production

❌ **DON'T:**
- Store tokens in `localStorage` (XSS risk)
- Expose client secrets in frontend code
- Disable CORS in production
- Use `http://` in production

---

## Next Steps

1. **Set up environment variables** for frontend:
   ```env
   REACT_APP_API_URL=http://localhost:9090
   REACT_APP_AUTH0_DOMAIN=dev-5nw6367bfpr277ec.us.auth0.com
   ```

2. **Add error handling** for API calls

3. **Implement logout** functionality

4. **Add loading states** for better UX

5. **Set up production build** with environment-specific configs

For more details, see:
- [Auth0 React SDK Docs](https://auth0.com/docs/quickstart/spa/react)
- [Spring Security OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
