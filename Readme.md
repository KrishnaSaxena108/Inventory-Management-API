# Inventory Management API

A RESTful Inventory Management System built using **Spring Boot**, **Spring Data JPA**, **Hibernate**, **MySQL**, **Lombok**, and **HikariCP**.

The application provides inventory tracking, stock management, supplier management, reorder alerts, and inventory validation to prevent negative stock levels.

---

# Features

## Product Management

* Create Product
* View Product
* Update Product
* Delete Product

## Supplier Management

* Create Supplier
* View Supplier
* Update Supplier
* Delete Supplier

## Inventory Management

* Add Stock
* Reduce Stock
* Receive Stock
* Check Current Inventory Level

## Business Rules

* Prevent Negative Inventory
* Reorder Alert Generation
* Inventory Tracking
* Supplier Association

---

# Tech Stack

| Technology      | Version                 |
| --------------- | ----------------------- |
| Java            | 17+                     |
| Spring Boot     | 3.x                     |
| Spring Data JPA | Latest                  |
| Hibernate       | ORM                     |
| MySQL           | 8.x                     |
| Lombok          | Latest                  |
| Maven           | Latest                  |
| HikariCP        | Default Connection Pool |

---

# Project Structure

```text
inventory-management-api
│
├── pom.xml
│
├── src
│   └── main
│       ├── java
│       │   └── com.inventory
│       │       ├── controller
│       │       ├── service
│       │       ├── repository
│       │       ├── entity
│       │       ├── dto
│       │       ├── exception
│       │       ├── util
│       │       └── InventoryApplication.java
│       │
│       └── resources
│           └── application.properties
│
└── README.md
```

---

# Database Setup

Login to MySQL:

```sql
mysql -u root -p
```

Create Database:

```sql
CREATE DATABASE inventory_db;
```

Verify:

```sql
SHOW DATABASES;
```

Use Database:

```sql
USE inventory_db;
```

---

# Configuration

Update `application.properties`

```properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/inventory_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

# Build Project

Clean Project:

```bash
mvn clean
```

Compile:

```bash
mvn compile
```

Package:

```bash
mvn clean install
```

---

# Run Application

Using Maven:

```bash
mvn spring-boot:run
```

Using JAR:

```bash
java -jar target/inventory-management-api-1.0.0.jar
```

Successful startup:

```text
Tomcat started on port(s): 8080
Started InventoryApplication
```

---

# API Endpoints

## Supplier APIs

### Create Supplier

POST

```http
/api/suppliers
```

Request:

```json
{
  "name":"Dell Supplier",
  "email":"dell@gmail.com",
  "phone":"9999999999"
}
```

---

### Get All Suppliers

GET

```http
/api/suppliers
```

---

### Get Supplier By ID

GET

```http
/api/suppliers/{id}
```

---

### Update Supplier

PUT

```http
/api/suppliers/{id}
```

---

### Delete Supplier

DELETE

```http
/api/suppliers/{id}
```

---

# Product APIs

### Create Product

POST

```http
/api/products
```

Request:

```json
{
  "name":"Dell Laptop",
  "description":"Latitude 7440",
  "reorderLevel":10,
  "supplierId":1
}
```

---

### Get All Products

GET

```http
/api/products
```

---

### Get Product By ID

GET

```http
/api/products/{id}
```

---

### Update Product

PUT

```http
/api/products/{id}
```

---

### Delete Product

DELETE

```http
/api/products/{id}
```

---

# Stock APIs

### Create Initial Stock

POST

```http
/api/stocks
```

Request:

```json
{
  "productId":1,
  "quantity":100
}
```

---

### Get All Stocks

GET

```http
/api/stocks
```

---

### Add Stock

PUT

```http
/api/stocks/add/{productId}?quantity=50
```

---

### Reduce Stock

PUT

```http
/api/stocks/reduce/{productId}?quantity=20
```

---

### Receive Inventory

PUT

```http
/api/stocks/receive/{productId}?quantity=100
```

---

### Check Inventory Level

GET

```http
/api/stocks/check/{productId}
```

---

### Reorder Alerts

GET

```http
/api/stocks/reorder-alerts
```

---

# Complete Workflow Testing

## Step 1

Create Supplier

```json
{
  "name":"Dell Supplier",
  "email":"dell@gmail.com",
  "phone":"9999999999"
}
```

Response:

```json
{
  "id":1
}
```

---

## Step 2

Create Product

```json
{
  "name":"Dell Laptop",
  "description":"Latitude 7440",
  "reorderLevel":10,
  "supplierId":1
}
```

Response:

```json
{
  "id":1
}
```

---

## Step 3

Create Stock

```json
{
  "productId":1,
  "quantity":100
}
```

---

## Step 4

Check Inventory

```http
GET /api/stocks/check/1
```

Expected:

```json
100
```

---

## Step 5

Reduce Stock

```http
PUT /api/stocks/reduce/1?quantity=95
```

Expected Inventory:

```json
5
```

---

## Step 6

Check Reorder Alert

```http
GET /api/stocks/reorder-alerts
```

Expected:

```json
[
  "REORDER REQUIRED : Dell Laptop Current Stock=5 Reorder Level=10"
]
```

---

## Step 7

Receive New Inventory

```http
PUT /api/stocks/receive/1?quantity=50
```

Expected Inventory:

```json
55
```

---

# Negative Inventory Validation

Current Inventory:

```json
10
```

Request:

```http
PUT /api/stocks/reduce/1?quantity=20
```

Response:

```json
{
  "status":400,
  "message":"Negative inventory not allowed"
}
```

This validation ensures stock quantity never becomes negative.

---

# Testing Using Terminal (cURL)

Create Supplier:

```bash
curl -X POST http://localhost:8080/api/suppliers ^
-H "Content-Type: application/json" ^
-d "{\"name\":\"Dell Supplier\",\"email\":\"dell@gmail.com\",\"phone\":\"9999999999\"}"
```

Get Suppliers:

```bash
curl http://localhost:8080/api/suppliers
```

Check Inventory:

```bash
curl http://localhost:8080/api/stocks/check/1
```

Reorder Alerts:

```bash
curl http://localhost:8080/api/stocks/reorder-alerts
```

---

# Business Rules Implemented

* Product CRUD
* Supplier CRUD
* Stock CRUD
* Inventory Tracking
* Reorder Alerts
* Stock Receiving
* Stock Reduction
* Negative Inventory Prevention
* Exception Handling
* Validation Support

---

# Future Enhancements

* Swagger/OpenAPI Documentation
* Docker Support
* JWT Authentication
* Role Based Authorization
* Inventory Transaction History
* Email Notifications
* Audit Logging
* Unit Testing with JUnit & Mockito
* Flyway Database Migrations
* Pagination and Sorting

---

# Author

Krishna

Inventory Management API developed using Spring Boot and MySQL for inventory tracking and stock management.