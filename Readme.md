<div align="center">

# 📦 Inventory Management API

**A production-ready RESTful API for managing products, suppliers, and stock — built with Spring Boot & MySQL.**

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

[Features](#-features) • [Tech Stack](#-tech-stack) • [Getting Started](#-getting-started) • [API Reference](#-api-reference) • [Workflow](#-end-to-end-workflow) • [Contributing](#-contributing)

</div>

---

## 📋 Overview

The **Inventory Management API** is a backend REST service designed to help businesses track products, manage suppliers, and control stock levels in real time. It enforces business rules like **negative stock prevention** and **automatic reorder alerts** to keep your inventory data accurate and reliable.

> Built with **Spring Boot 3**, **Spring Data JPA**, **Hibernate ORM**, **MySQL 8**, **Lombok**, and **HikariCP** connection pooling.

---

## ✨ Features

| Module | Capabilities |
|---|---|
| 🏷️ **Product Management** | Create, Read, Update, Delete products |
| 🏭 **Supplier Management** | Create, Read, Update, Delete suppliers |
| 📊 **Stock Management** | Add, reduce, receive stock; check inventory levels |
| 🔔 **Reorder Alerts** | Auto-flag products that fall below reorder threshold |
| 🛡️ **Inventory Validation** | Prevents stock from going negative — ever |
| ⚡ **Connection Pooling** | HikariCP for high-performance DB connections |

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17+ | Core language |
| Spring Boot | 3.x | Application framework |
| Spring Data JPA | Latest | Data access layer |
| Hibernate | ORM | Object-relational mapping |
| MySQL | 8.x | Relational database |
| Lombok | Latest | Boilerplate reduction |
| HikariCP | Default | Connection pooling |
| Maven | Latest | Build & dependency management |

---

## 🗂️ Project Structure

```
inventory-management-api/
│
├── pom.xml                         # Maven dependencies & build config
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/inventory/
│       │       ├── controller/     # REST controllers (API layer)
│       │       ├── service/        # Business logic layer
│       │       ├── repository/     # JPA repositories (DB layer)
│       │       ├── entity/         # JPA entity classes
│       │       ├── dto/            # Data Transfer Objects
│       │       ├── exception/      # Custom exception handlers
│       │       ├── util/           # Utility/helper classes
│       │       └── InventoryApplication.java
│       │
│       └── resources/
│           └── application.properties   # App & DB configuration
│
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

- ☕ [Java 17+](https://adoptium.net/)
- 🐬 [MySQL 8.x](https://dev.mysql.com/downloads/)
- 📦 [Maven 3.x](https://maven.apache.org/download.cgi)
- 🛠️ [Git](https://git-scm.com/)

---

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/KrishnaSaxena108/Inventory-Management-API.git
cd Inventory-Management-API
```

---

### 2️⃣ Set Up the Database

Log into MySQL and create the database:

```sql
mysql -u root -p

CREATE DATABASE inventory_db;
SHOW DATABASES;   -- verify it's there
USE inventory_db;
```

---

### 3️⃣ Configure the Application

Edit `src/main/resources/application.properties`:

```properties
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> ⚠️ **Never commit real credentials.** Use environment variables or a `.env` file in production.

---

### 4️⃣ Build & Run

**Build the project:**

```bash
mvn clean install
```

**Run with Maven:**

```bash
mvn spring-boot:run
```

**Or run the JAR directly:**

```bash
java -jar target/inventory-management-api-1.0.0.jar
```

**Successful startup looks like:**

```
Tomcat started on port(s): 8080
Started InventoryApplication in X.XXX seconds
```

The API is now live at: **`http://localhost:8080`**

---

## 📡 API Reference

Base URL: `http://localhost:8080`

---

### 🏭 Supplier Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/suppliers` | Create a new supplier |
| `GET` | `/api/suppliers` | Get all suppliers |
| `GET` | `/api/suppliers/{id}` | Get supplier by ID |
| `PUT` | `/api/suppliers/{id}` | Update supplier |
| `DELETE` | `/api/suppliers/{id}` | Delete supplier |

**Create Supplier — Request Body:**

```json
{
  "name": "Dell Supplier",
  "email": "dell@gmail.com",
  "phone": "9999999999"
}
```

---

### 🏷️ Product Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/products` | Create a new product |
| `GET` | `/api/products` | Get all products |
| `GET` | `/api/products/{id}` | Get product by ID |
| `PUT` | `/api/products/{id}` | Update product |
| `DELETE` | `/api/products/{id}` | Delete product |

**Create Product — Request Body:**

```json
{
  "name": "Dell Laptop",
  "description": "Latitude 7440",
  "reorderLevel": 10,
  "supplierId": 1
}
```

---

### 📊 Stock Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/stocks` | Initialize stock for a product |
| `GET` | `/api/stocks` | Get all stock records |
| `PUT` | `/api/stocks/add/{productId}?quantity=N` | Add stock |
| `PUT` | `/api/stocks/reduce/{productId}?quantity=N` | Reduce stock |
| `PUT` | `/api/stocks/receive/{productId}?quantity=N` | Receive new inventory |
| `GET` | `/api/stocks/check/{productId}` | Check current stock level |
| `GET` | `/api/stocks/reorder-alerts` | Get all products needing reorder |

**Initialize Stock — Request Body:**

```json
{
  "productId": 1,
  "quantity": 100
}
```

---

## 🔄 End-to-End Workflow

Here's a full example to test the system from scratch:

```bash
# Step 1 — Create a supplier
curl -X POST http://localhost:8080/api/suppliers \
  -H "Content-Type: application/json" \
  -d '{"name":"Dell Supplier","email":"dell@gmail.com","phone":"9999999999"}'

# Step 2 — Create a product linked to supplier ID 1
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Dell Laptop","description":"Latitude 7440","reorderLevel":10,"supplierId":1}'

# Step 3 — Initialize stock (100 units)
curl -X POST http://localhost:8080/api/stocks \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":100}'

# Step 4 — Check inventory
curl http://localhost:8080/api/stocks/check/1
# Expected: 100

# Step 5 — Reduce stock by 95
curl -X PUT "http://localhost:8080/api/stocks/reduce/1?quantity=95"
# Expected: 5

# Step 6 — Check reorder alert (stock 5 < reorderLevel 10)
curl http://localhost:8080/api/stocks/reorder-alerts
# Expected: ["REORDER REQUIRED : Dell Laptop Current Stock=5 Reorder Level=10"]

# Step 7 — Receive new inventory
curl -X PUT "http://localhost:8080/api/stocks/receive/1?quantity=50"
# Expected: 55
```

---

## 🛡️ Business Rules

### Negative Inventory Prevention

Attempting to reduce stock below zero returns a `400 Bad Request`:

```bash
# Current stock: 10 — trying to reduce by 20
curl -X PUT "http://localhost:8080/api/stocks/reduce/1?quantity=20"
```

```json
{
  "status": 400,
  "message": "Negative inventory not allowed"
}
```

### Reorder Alert Logic

When `currentStock < reorderLevel`, the product is flagged automatically in the `/api/stocks/reorder-alerts` endpoint — no manual checks needed.

---

## 🗺️ Roadmap

- [ ] Swagger / OpenAPI documentation
- [ ] Docker & Docker Compose support
- [ ] JWT Authentication & role-based authorization
- [ ] Inventory transaction history & audit logging
- [ ] Email notifications for reorder alerts
- [ ] Unit & integration tests (JUnit 5 + Mockito)
- [ ] Flyway database migrations
- [ ] Pagination and sorting on list endpoints
- [ ] CI/CD pipeline (GitHub Actions)

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/your-feature-name`
3. **Commit** your changes: `git commit -m 'feat: add your feature'`
4. **Push** to the branch: `git push origin feature/your-feature-name`
5. **Open** a Pull Request

Please follow [Conventional Commits](https://www.conventionalcommits.org/) for commit messages.

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Krishna Saxena**

[![GitHub](https://img.shields.io/badge/GitHub-KrishnaSaxena108-181717?style=flat&logo=github)](https://github.com/KrishnaSaxena108)

---

<div align="center">

⭐ **If this project helped you, give it a star!** ⭐

</div>
