# E-Commerce Order Management System

A backend **microservices-based E-Commerce Order Management System** built with **Java 21, Spring Boot, Spring Cloud, MongoDB, Eureka, OpenFeign, Resilience4j, and Swagger/OpenAPI**.

The system is designed around independent services for product management and order management, with **API Gateway** as the single entry point and **Eureka Service Registry** for service discovery.

---

## 🏗️ Architecture

```text
                         ┌──────────────────────┐
                         │       Client         │
                         │  Postman / Browser   │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │     API Gateway      │
                         │       :8080          │
                         └──────────┬───────────┘
                                    │
                     ┌──────────────┴──────────────┐
                     │                             │
                     ▼                             ▼
          ┌────────────────────┐       ┌────────────────────┐
          │   Product Service  │       │    Order Service   │
          │       :8081        │◄──────│       :8082        │
          └─────────┬──────────┘ Feign └─────────┬──────────┘
                    │                            │
                    ▼                            ▼
          ┌────────────────────┐       ┌────────────────────┐
          │  Product MongoDB   │       │   Order MongoDB    │
          │ ecommerce_product  │       │   ecommerce_order  │
          │       _db          │       │        _db         │
          └────────────────────┘       └────────────────────┘

                         ┌──────────────────────┐
                         │   Eureka Registry    │
                         │       :8761          │
                         └──────────────────────┘
                                  ▲
                    ┌─────────────┼─────────────┐
                    │             │             │
                Gateway       Product        Order
                Service       Service        Service
```
![E-Commerce Microservices Architecture](architecture.png)
---
## 📸 Screenshots

### Eureka Service Registry

Shows all microservices registered and running successfully.
![Eureka Dashboard](screenshots/eureka-dashboard.png)

### Product Service — Swagger

Interactive Swagger documentation for Product Service APIs.
![Product Swagger](screenshots/product-swagger.png)

### Order Service — Swagger

Interactive Swagger documentation for Order Service APIs.
![Order Swagger](screenshots/order-swagger.png)

### Create Order — Postman

Successful order creation through the API Gateway.
![Create Order](screenshots/create-order.png)
---

## ✨ Features

* Product CRUD and search APIs
* Order creation and retrieval
* Order status management
* Order cancellation
* Product stock reservation
* Product stock release
* Inter-service communication using OpenFeign
* Eureka-based service registration and discovery
* API Gateway for centralized API access
* MongoDB persistence
* Request validation
* Global exception handling
* Swagger/OpenAPI documentation
* Spring Boot Actuator health endpoints
* Resilience4j Circuit Breaker for Product Service communication
* Graceful `503 Service Unavailable` responses when dependent services are unavailable
* Maven-based project management

---

## 🧩 Microservices

### 1. Service Registry

**Port:** `8761`

Technology:

* Spring Boot
* Spring Cloud Netflix Eureka Server

Responsibilities:

* Service registration
* Service discovery
* Maintaining the list of available microservice instances

Eureka Dashboard:

```text
http://localhost:8761
```

---

### 2. Product Service

**Port:** `8081`

Technology:

* Spring Boot
* Spring Data MongoDB
* MongoDB
* Validation
* Eureka Client
* Swagger/OpenAPI
* Actuator

Responsibilities:

* Create products
* Retrieve products
* Search/filter products
* Manage inventory
* Reserve stock
* Release stock

Database:

```text
ecommerce_product_db
```

---

### 3. Order Service

**Port:** `8082`

Technology:

* Spring Boot
* Spring Data MongoDB
* OpenFeign
* Eureka Client
* Resilience4j
* Validation
* Swagger/OpenAPI
* Actuator

Responsibilities:

* Create orders
* Retrieve orders
* Retrieve orders by order number
* Cancel orders
* Update order status
* Calculate order totals
* Communicate with Product Service
* Reserve and release product stock

Database:

```text
ecommerce_order_db
```

---

### 4. API Gateway

**Port:** `8080`

Responsibilities:

* Single entry point for clients
* Routes product requests to Product Service
* Routes order requests to Order Service
* Prevents clients from directly depending on individual service endpoints

Gateway endpoints:

```text
/api/v1/products/**
/api/v1/orders/**
```

---

# 🛠️ Technology Stack

| Technology                  | Purpose                         |
| --------------------------- | ------------------------------- |
| Java 21                     | Programming language            |
| Spring Boot 4.0.7           | Application framework           |
| Spring Cloud 2025.1.2       | Microservices infrastructure    |
| Spring Cloud Netflix Eureka | Service discovery               |
| Spring Cloud Gateway        | API Gateway                     |
| Spring Cloud OpenFeign      | Inter-service communication     |
| Resilience4j                | Circuit breaker                 |
| Spring Data MongoDB         | Database access                 |
| MongoDB                     | NoSQL database                  |
| Swagger / OpenAPI           | API documentation               |
| Spring Boot Actuator        | Health monitoring               |
| Maven                       | Build and dependency management |
| Git / GitHub                | Version control                 |
| Postman                     | API testing                     |

---

# 📁 Project Structure

```text
ecommerce-microservices/
│
├── service-registry/
│   ├── src/
│   ├── pom.xml
│   └── mvnw.cmd
│
├── product-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   ├── pom.xml
│   └── mvnw.cmd
│
├── order-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   ├── pom.xml
│   └── mvnw.cmd
│
├── api-gateway/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   ├── pom.xml
│   └── mvnw.cmd
│
└── README.md
```

---

# 🔄 Order Processing Flow

When a client creates an order:

```text
Client
   │
   │ POST /api/v1/orders
   ▼
API Gateway
   │
   ▼
Order Service
   │
   │ 1. Request product details
   ▼
Product Service
   │
   │ 2. Return product information
   ▼
Order Service
   │
   │ 3. Reserve stock
   ▼
Product Service
   │
   │ 4. Stock successfully reserved
   ▼
Order Service
   │
   │ 5. Calculate total
   │
   │ 6. Save order
   ▼
MongoDB
```

The order stores:

* Product ID
* Product name
* Product price
* Quantity
* Item subtotal
* Total order amount
* Customer ID
* Order number
* Order status
* Payment status
* Creation timestamp

---

# 📦 Order Status Lifecycle

Orders follow a controlled status transition:

```text
CREATED
   │
   ▼
CONFIRMED
   │
   ▼
PROCESSING
   │
   ▼
SHIPPED
   │
   ▼
DELIVERED
```

Cancellation is supported from appropriate intermediate states:

```text
CREATED ──────► CANCELLED
CONFIRMED ────► CANCELLED
PROCESSING ───► CANCELLED
```

Invalid transitions are rejected by the Order Service.

For example:

```text
CREATED → SHIPPED
```

is not allowed.

---

# 🔗 Service Communication

Order Service communicates with Product Service using **Spring Cloud OpenFeign**.

Example:

```java
@FeignClient(name = "product-service")
public interface ProductClient {
}
```

The Order Service uses Product Service for:

```text
GET  /api/v1/products/{id}
PUT  /api/v1/products/{id}/reserve-stock
PUT  /api/v1/products/{id}/release-stock
```

Eureka provides service discovery for the `product-service` application.

---

# 🛡️ Resilience4j Circuit Breaker

The Order Service uses **Resilience4j Circuit Breaker** around Product Service calls.

This prevents failures in Product Service from unnecessarily propagating through the entire application.

Example behavior:

```text
Order Service
     │
     ▼
Product Service
     │
     X unavailable
     │
     ▼
Circuit Breaker
     │
     ▼
503 Service Unavailable
```

Instead of exposing a raw Feign/network exception, the application returns a controlled response such as:

```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "Product Service is currently unavailable. Please try again later."
}
```

This improves fault tolerance and provides a better API experience.

---

# 🗄️ MongoDB Configuration

Product Service:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/ecommerce_product_db
```

Order Service:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/ecommerce_order_db
```

MongoDB must be running locally before starting the services.

---

# 🚀 Getting Started

## Prerequisites

Install:

* Java 21
* Maven
* MongoDB
* Git

Verify Java:

```powershell
java -version
```

Verify Maven:

```powershell
mvn -version
```

Verify MongoDB is running on:

```text
localhost:27017
```

---

# ▶️ Running the Application

Start the services in this order.

## 1. Start Eureka Service Registry

```powershell
cd service-registry
.\mvnw.cmd spring-boot:run
```

Open:

```text
http://localhost:8761
```

---

## 2. Start Product Service

```powershell
cd product-service
.\mvnw.cmd spring-boot:run
```

Runs on:

```text
http://localhost:8081
```

---

## 3. Start Order Service

```powershell
cd order-service
.\mvnw.cmd spring-boot:run
```

Runs on:

```text
http://localhost:8082
```

---

## 4. Start API Gateway

```powershell
cd api-gateway
.\mvnw.cmd spring-boot:run
```

Runs on:

```text
http://localhost:8080
```

---

# 🌐 API Endpoints

All client requests should preferably go through:

```text
http://localhost:8080
```

## Product APIs

### Create Product

```http
POST /api/v1/products
```

Example:

```json
{
  "sku": "PHONE-001",
  "name": "Smartphone",
  "category": "Electronics",
  "description": "Test smartphone",
  "price": 25000,
  "stock": 10,
  "active": true
}
```

### Get Product

```http
GET /api/v1/products/{id}
```

### Search Products

```http
GET /api/v1/products
```

Supported query parameters include:

```text
category
search
minPrice
maxPrice
```

Example:

```http
GET /api/v1/products?category=Electronics&minPrice=10000&maxPrice=50000
```

---

# 🛒 Order APIs

### Create Order

```http
POST /api/v1/orders
```

Example:

```json
{
  "customerId": "CUST-TEST-001",
  "items": [
    {
      "productId": "PRODUCT_ID",
      "quantity": 2
    }
  ]
}
```

### Get Order by ID

```http
GET /api/v1/orders/{id}
```

### Get Order by Order Number

```http
GET /api/v1/orders/number/{orderNumber}
```

### Update Order Status

```http
PUT /api/v1/orders/{id}/status
```

### Cancel Order

```http
PUT /api/v1/orders/{id}/cancel
```

---

# 📖 Swagger / OpenAPI

Swagger is integrated into the Product Service and Order Service.

Product Service:

```text
http://localhost:8081/swagger-ui/index.html
```

Order Service:

```text
http://localhost:8082/swagger-ui/index.html
```

Swagger provides an interactive interface for exploring and testing the APIs.

---

# ❤️ Health Monitoring

Spring Boot Actuator is enabled for service monitoring.

Product Service:

```text
http://localhost:8081/actuator/health
```

Order Service:

```text
http://localhost:8082/actuator/health
```

API Gateway:

```text
http://localhost:8080/actuator/health
```

---

# 🧪 Testing

The application was tested using Postman.

### Core scenarios tested

* Product creation
* Product retrieval
* Product search
* Order creation
* Order retrieval
* Order cancellation
* Order status updates
* Stock reservation
* Stock release
* Invalid product ID
* Invalid order ID
* Invalid order status transition
* Product Service unavailable
* Circuit breaker fallback
* API Gateway routing
* Eureka service registration

### Service failure testing

When Product Service is unavailable, Order Service handles the failure through Resilience4j and returns a controlled `503 Service Unavailable` response.

---

# 🔐 Error Handling

The Order Service contains centralized exception handling using `@RestControllerAdvice`.

Examples:

### Order not found

```text
HTTP 404 NOT FOUND
```

### Dependent service unavailable

```text
HTTP 503 SERVICE UNAVAILABLE
```

This prevents raw internal exceptions from being exposed directly to API clients.

---

# 🔍 Example End-to-End Request

Create a product:

```text
POST http://localhost:8080/api/v1/products
```

Then create an order using the returned product ID:

```text
POST http://localhost:8080/api/v1/orders
```

The system:

```text
API Gateway
     ↓
Order Service
     ↓
Product Service
     ↓
Reserve Stock
     ↓
Calculate Order Total
     ↓
Save Order in MongoDB
     ↓
Return Order Response
```

---

# 🧰 Build

Build Product Service:

```powershell
cd product-service
.\mvnw.cmd clean package
```

Build Order Service:

```powershell
cd order-service
.\mvnw.cmd clean package
```

Build API Gateway:

```powershell
cd api-gateway
.\mvnw.cmd clean package
```

Build Service Registry:

```powershell
cd service-registry
.\mvnw.cmd clean package
```

---

# 📌 Design Highlights

### Microservice separation

Product and Order functionality are separated into independently deployable services.

### Service discovery

Eureka allows services to register and discover one another without hardcoding service instance information for inter-service communication.

### API Gateway

Clients interact with a single gateway instead of communicating directly with every microservice.

### Declarative REST communication

OpenFeign simplifies communication between Order Service and Product Service.

### Fault tolerance

Resilience4j prevents temporary Product Service failures from causing uncontrolled failures in Order Service.

### Data isolation

Each service has its own MongoDB database:

```text
Product Service → ecommerce_product_db
Order Service   → ecommerce_order_db
```

---

# 📈 Future Improvements

Potential future enhancements include:

* Authentication and authorization with Spring Security + JWT
* Payment Service
* Notification Service
* Kafka-based asynchronous communication
* Distributed tracing
* Centralized configuration
* Centralized logging
* Redis caching
* Docker/containerization
* Automated CI/CD pipeline
* Automated integration tests
* Kubernetes deployment

---

# 🎯 Key Learning Outcomes

This project demonstrates practical experience with:

* Java backend development
* Spring Boot
* REST API development
* MongoDB
* Microservice architecture
* Service discovery
* API Gateway
* OpenFeign
* Circuit breaker pattern
* Exception handling
* API validation
* Swagger/OpenAPI
* Actuator monitoring
* Maven
* Git/GitHub
* Postman API testing

---

# 👩‍💻 Author

**Rakshitha Bai V.**

Computer Science & Engineering — Data Science

Built as a practical Java Spring Boot microservices project demonstrating backend development, distributed service communication, service discovery, API gateway architecture, and fault-tolerant design.
