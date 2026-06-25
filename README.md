# Ecom - Monolithic E-Commerce Backend

A Spring Boot based monolithic e-commerce backend application that provides user management, product catalog management, shopping cart functionality, and order processing within a single deployable service.

---

## Features

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
│   └── AddressDTO
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

**Create Product:**
`POST /api/products`

**Get All Active Products:**
`GET /api/products`

**Update Product:**
`PUT /api/products/{id}`

**Delete Product (Soft Delete):**
`DELETE /api/products/{id}`

**Search Products:**
`GET /api/products/search?keyword=laptop`

---

### Cart APIs

All cart requests require `X-User-ID` header.

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

Header:
`X-User-ID: 1`

**Remove Product From Cart:**
`DELETE /api/cart/items/{productId}`

Example:
`DELETE /api/cart/items/5`

---

### Order APIs

All order requests require `X-User-ID` header.

**Create Order:**
`POST /api/orders`

Header:
`X-User-ID: 1`

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

## Database Configuration

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/Ecom
spring.datasource.username=postgres
spring.datasource.password=jdbc@admin

spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

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

---

## Sample Business Flow

**Step 1: Create User**
`POST /api/users`

**Step 2: Create Product**
`POST /api/products`

**Step 3: Add Product To Cart**
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

- JWT Authentication
- Role-Based Access Control (RBAC)
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
- Unit and Integration Tests

---

## License

This project is intended for educational and learning purposes and demonstrates a complete monolithic e-commerce backend architecture using Spring Boot, JPA, PostgreSQL, and REST APIs.
