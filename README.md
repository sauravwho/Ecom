# Ecom - Monolithic E-Commerce Backend

A Spring Boot based monolithic e-commerce backend application that provides user management, product catalog management, shopping cart functionality, and order processing within a single deployable service.

---

## Features

### Authentication & Authorization

- User registration and login with JWT-based authentication
- Passwords hashed with BCrypt, never stored or returned in plaintext
- Stateless sessions — every request after login is authenticated via a `Bearer` token
- Role-based access control: `CUSTOMER` and `ADMIN` roles, enforced with `@PreAuthorize`
- Cart and order endpoints derive the user's identity from their JWT, not a client-supplied header

### User Management

- Create users
- Retrieve all users
- Retrieve user by ID
- Update user information
- Manage user addresses
- Support user roles

**User Fields:**

- First Name
- Last Name
- Email
- Phone Number
- Address
- Role

**Address Fields:**

- Street
- City
- State
- Country
- Zip Code

---

### Product Management

- Create products
- Retrieve active products
- Update product information
- Soft delete products
- Search products by keyword
- Manage inventory stock
- Manage pricing
- Store product image URLs

**Product Fields:**

- Product Name
- Description
- Category
- Price
- Stock Quantity
- Image URL
- Active Status

---

### Shopping Cart

- Add products to cart
- View cart contents
- Remove products from cart
- Quantity management
- Automatic price calculation
- Stock validation before adding items

**Cart Validation:**

- User must exist
- Product must exist
- Requested quantity must be available

**Price Calculation:**

`Line Total = Product Price × Quantity`

---

### Order Processing

- Create orders from cart items
- Calculate order totals
- Generate order items
- Persist order history
- Automatically clear cart after checkout

**Order Flow:**

1. User adds products to cart
2. User places an order
3. Cart validation occurs
4. Total amount is calculated
5. Order is created
6. Order items are generated
7. Cart is cleared
8. Order status becomes `CONFIRMED`

---

### Product Search

Search products using keywords across:

- Product Name
- Product Description
- Product Category

---

## Technology Stack

**Backend:**

- Java 25
- Spring Boot 4
- Spring MVC
- Spring Data JPA
- Hibernate ORM
- Spring Security
- JJWT (JSON Web Tokens)

**Database:**

- PostgreSQL

**Documentation:**

- OpenAPI
- Swagger UI

**Utilities:**

- Lombok

**Monitoring:**

- Spring Boot Actuator

---

## Project Structure

```
src/main/java/com/example/ecom
├── controller
│   ├── UserController
│   ├── ProductController
│   ├── CartController
│   └── OrderController
│
├── service
│   ├── UserService
│   ├── ProductService
│   ├── CartService
│   └── OrderService
│
├── repository
│   ├── UserRepository
│   ├── ProductRepository
│   ├── CartItemRepository
│   └── OrderRepository
│
├── dto
│   ├── UserRequest
│   ├── UserResponse
│   ├── ProductRequest
│   ├── ProductResponse
│   ├── OrderResponse
│   ├── AddressDTO
│   ├── RegisterRequest
│   ├── LoginRequest
│   └── AuthResponse
│
├── security
│   ├── SecurityConfig
│   ├── JwtUtil
│   ├── JwtAuthenticationFilter
│   ├── UserPrincipal
│   └── CustomUserDetailsService
│
└── data
    ├── User
    ├── Address
    ├── Product
    ├── CartItem
    ├── Order
    ├── OrderItem
    ├── UserRole
    └── OrderStatus
```

---

## API Endpoints

### Auth APIs

**Register:**
`POST /api/auth/register`

Request Body:
```json
{
  "firstName": "Saurav",
  "lastName": "Kumar",
  "email": "saurav@example.com",
  "phone": "9999999999",
  "password": "SecurePass123"
}
```

Response includes a JWT — registration logs you in immediately. New accounts are always created with role `CUSTOMER`.

**Login:**
`POST /api/auth/login`

Request Body:
```json
{
  "email": "saurav@example.com",
  "password": "SecurePass123"
}
```

Response:
```json
{
  "token": "<jwt>",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "saurav@example.com",
  "role": "CUSTOMER"
}
```

For every endpoint below except `/api/auth/**`, Swagger, and `/actuator/health`, send the token on subsequent requests:
`Authorization: Bearer <jwt>`

---

### User APIs

**Create User:**
`POST /api/users`

**Get All Users:**
`GET /api/users`

**Get User By ID:**
`GET /api/users/{id}`

**Update User:**
`PUT /api/users/{id}`

---

### Product APIs

**Create Product** *(ADMIN only)*:
`POST /api/products`

**Get All Active Products:**
`GET /api/products`

**Update Product** *(ADMIN only)*:
`PUT /api/products/{id}`

**Delete Product (Soft Delete)** *(ADMIN only)*:
`DELETE /api/products/{id}`

**Search Products:**
`GET /api/products/search?keyword=laptop`

---

### Cart APIs

All cart requests require a valid JWT (`Authorization: Bearer <jwt>`). The user is identified from the token — there is no client-supplied user ID.

**Add Product To Cart:**
`POST /api/cart`

Request Body:
```json
{
  "productId": 1,
  "quantity": 2
}
```

**View Cart:**
`GET /api/cart`

**Remove Product From Cart:**
`DELETE /api/cart/items/{productId}`

Example:
`DELETE /api/cart/items/5`

---

### Order APIs

All order requests require a valid JWT (`Authorization: Bearer <jwt>`).

**Create Order:**
`POST /api/orders`

Response:
- Order ID
- Total Amount
- Order Status
- Order Items
- Created Timestamp

---

## API Documentation

**Swagger UI:**
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/swagger-ui/index.html`

---

## Monitoring

**Health Check Endpoint:**
`GET /actuator/health`

**Additional Actuator Features:**
- Health
- Metrics
- Environment
- Beans
- Monitoring Information

---

## Database & JWT Configuration

The application uses environment variables for sensitive configurations. A template file `application.properties.example` is provided for local development.

Example configuration in `src/main/resources/application.properties.example`:
```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/Ecom}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:your_db_password}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

jwt.secret=${JWT_SECRET:your_jwt_secret}
jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}
```

**Important:**
- Create a file named `application.properties` in `src/main/resources/` (it's ignored by Git).
- Copy the content from `application.properties.example` into `application.properties`.
- **Replace `your_db_password` and `your_jwt_secret` with actual secure values.**
- For non-local environments (e.g., production), always set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` via environment variables. Never commit actual secrets to source control.

---

## Running the Application

### Prerequisites

- Java 25
- Maven
- PostgreSQL

### Create Database

```sql
CREATE DATABASE Ecom;
```

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```
or
```bash
java -jar target/Ecom-0.0.1-SNAPSHOT.jar
```

### Run Tests

```bash
mvn clean test
```

Tests run against an in-memory H2 database (configured in `src/test/resources/application.properties`), so no local PostgreSQL instance is needed to run the suite. Includes unit tests (Mockito) for the service layer and full integration tests (MockMvc) covering registration, login, role-based access control, and the end-to-end cart-to-checkout flow.

---

## Sample Business Flow

**Step 1: Register**
`POST /api/auth/register` → returns a JWT

**Step 2: Create Product** *(ADMIN token required)*
`POST /api/products`

**Step 3: Add Product To Cart** *(customer token)*
`POST /api/cart`

**Step 4: View Cart**
`GET /api/cart`

**Step 5: Place Order**
`POST /api/orders`

**Result:**
The system:
- Calculates total amount
- Creates order items
- Stores the order
- Clears the cart
- Returns confirmed order details

---

## Future Enhancements

- Payment Gateway Integration
- Order History API
- Order Status Tracking
- Inventory Reservation
- Pagination and Sorting
- Redis Caching
- Docker Support
- CI/CD Pipeline
- Email Notifications
- Audit Logging
- API Rate Limiting
- Refresh tokens / token revocation

---

## License

This project is intended for educational and learning purposes and demonstrates a complete monolithic e-commerce backend architecture using Spring Boot, Spring Data JPA, PostgreSQL, and REST APIs.