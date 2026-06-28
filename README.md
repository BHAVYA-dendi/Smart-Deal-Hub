# 🛍️ Smart Deal Hub

A Spring Boot–based web application that enables users to discover product deals, compare prices, manage favourites, participate in group deals, and interact with stores through a secure RESTful backend.

This project follows a layered architecture using Spring Boot, Spring Security, JPA, and MySQL, making it suitable for learning enterprise Java application development.

---

# ✨ Features

### 👤 User Management

* User Registration & Login
* JWT-based Authentication
* Password Management
* User Activity Tracking

### 🛒 Product Management

* Browse Products
* Product Details
* Product Price Comparison
* Price History Tracking

### 🤝 Group Deals

* Create Group Deals
* Join Existing Group Deals
* Invite Members
* Track Group Members

### ❤️ Favourite Products

* Save Favourite Products
* View Favourite List
* Remove Favourites

### 🏪 Store Management

* Store Registration
* Store Information
* Store Working Hours
* Owner Dashboard

### ⭐ Reviews & Feedback

* Product Reviews
* Store Feedback
* User Ratings

### 🔔 Notifications

* User Notifications
* Deal Invitations
* Push Notification Support

### 🔒 Security

* Spring Security
* JWT Authentication
* Protected REST APIs

### 📄 API Documentation

* Swagger/OpenAPI Integration

---

# 🛠️ Tech Stack

## Backend

* Java
* Spring Boot
* Spring MVC
* Spring Security
* Spring Data JPA
* Hibernate

## Database

* MySQL

## Build Tool

* Maven

## Authentication

* JSON Web Token (JWT)

## API Documentation

* Swagger / OpenAPI

---

# 📂 Project Structure

```text
Smart-Deal-Hub
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.smartdealhub.smartdealhub
│   │   │       ├── config
│   │   │       ├── controller
│   │   │       ├── dto
│   │   │       ├── model
│   │   │       ├── repository
│   │   │       ├── security
│   │   │       ├── service
│   │   │       └── SmartdealhubApplication.java
│   │   │
│   │   └── resources
│   │       ├── application.properties
│   │       └── static
│   │           └── index.html
│   │
│   └── test
│
├── pom.xml
├── mvnw
└── README.md
```

---

# 🚀 Getting Started

## 1. Clone the Repository

```bash
git clone https://github.com/BHAVYA-dendi/Smart-Deal-Hub.git
```

## 2. Navigate to the Project

```bash
cd Smart-Deal-Hub
```

## 3. Configure Database

Update the database configuration inside:

```text
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smartdealhub
spring.datasource.username=your_username
spring.datasource.password=your_password
```

---

## 4. Install Dependencies

```bash
mvn clean install
```

---

## 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

---

# 📌 Main Modules

* User Management
* Product Management
* Group Deal Management
* Favourite Products
* Reviews & Ratings
* Store Management
* Notifications
* Security & Authentication

---

# 📸 Screenshots

Screenshots will be added after deployment.

Example:

```text
docs/screenshots/
├── home.png
├── login.png
├── products.png
├── group-deals.png
├── dashboard.png
```

---

# 🔮 Future Improvements

* Responsive Frontend
* Email Notifications
* Google OAuth Login
* Product Recommendation System
* Real-time Deal Alerts
* Payment Gateway Integration
* Docker Deployment
* Cloud Deployment

---

# 📚 Learning Outcomes

This project helped in understanding:

* Spring Boot Application Development
* REST API Development
* Spring Security
* JWT Authentication
* Layered Architecture
* Spring Data JPA
* Repository Pattern
* Maven Project Management
* Backend Development using Java

---

# ⚠️ Disclaimer

This project was developed for academic and learning purposes.

Some features are prototype implementations intended to demonstrate backend architecture and REST API development.

---

# 👨‍💻 Author

**Dendi Bhavya Reddy**

GitHub: https://github.com/BHAVYA-dendi
