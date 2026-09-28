# 🍲 FoodShare — Surplus Food Donation Matching Platform

A robust Spring Boot backend platform designed to connect campus canteens and caterers (Donors) with local shelters and orphanages (NGOs) to divert surplus food from going to waste.

---

## 🚀 Key Features

1. **Donor Food Listing:** Donors can list surplus food specifying quantity, food type (VEG/NON_VEG/COOKED/RAW), and a strict safe-to-eat-until expiry window.
2. **NGO Browse & Claim:** NGOs can browse available surplus listings in real time and claim them before they spoil.
3. **Food Collection Tracking:** Claims are marked as collected once picked up, updating timestamps and diversion records.
4. **Automated Expiry Scheduler:** A background cron task (`@Scheduled`) checks listings every minute and automatically marks unclaimed listings that passed their safe-to-eat window as `EXPIRED`.
5. **Monthly Waste Diversion Analytics:** Aggregates and reports total food quantity saved from waste grouped by units and month.
6. **Service Layer Business Rules:**
   - Listings past safe-to-eat-until time cannot be claimed (`ListingExpiredException`).
   - Only one NGO can hold an active claim on any listing at a time (`ListingAlreadyClaimedException`).
7. **Clean REST Architecture:** Centralized error handling via `@RestControllerAdvice` returning standard RFC-compliant JSON responses instead of raw stack traces.
8. **Built-in Web Test Dashboard:** Interactive UI served at `http://localhost:8080/` to test all flows right from your browser.

---

## 🛠️ Technology Stack

* **Language:** Java 17 LTS
* **Framework:** Spring Boot 4.x / 3.x (Spring Web, Spring Data JPA, Spring Validation)
* **Database:** MySQL 8.0
* **ORM:** Hibernate 7
* **Build Tool:** Maven (with Maven Wrapper `mvnw`)
* **Testing:** JUnit 5, Spring Boot Test

---

## 🏛️ Database Schema & Entities

* **`donors`**: Stores donor name, verified email, phone, and address.
* **`ngos`**: Stores shelter details, contact person, verified email, phone, and address.
* **`food_listings`**: Stores food details, quantity, unit, safe-to-eat-until timestamp, status (`AVAILABLE`, `CLAIMED`, `COLLECTED`, `EXPIRED`), linked to `donors`.
* **`claims`**: Tracks food claims, linked to `food_listings` and `ngos`, with status (`ACTIVE`, `COLLECTED`, `CANCELLED`) and collection timestamps.

---

## 🔌 API Endpoints Summary

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/listings` | Create a new surplus food listing (Donor) |
| `GET` | `/api/listings/available` | Browse available listings (NGO) |
| `GET` | `/api/listings` | View all listings |
| `POST` | `/api/claims` | Claim an available listing (NGO) |
| `PUT` | `/api/claims/{id}/collect` | Mark a claimed listing as collected |
| `GET` | `/api/claims` | View all claim records |
| `GET` | `/api/analytics/monthly-diverted` | View monthly food waste diversion report |
| `GET` | `/api/donors` | List registered donors |
| `GET` | `/api/ngos` | List registered NGOs |

---

## 🏃 Running the Application

### 1. Database Setup
Ensure MySQL is running on `localhost:3306`.
Create database (automatically handled if permitted):
```sql
CREATE DATABASE foodshare_db;
```
Configure credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/foodshare_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

### 2. Start the Server
Run using the included Maven wrapper:
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```

### 3. Access the Dashboard
Open your browser and navigate to:
```
http://localhost:8080/
```

### 4. Running Automated Tests
Execute the full test suite verifying all 5 core features and business rules:
```bash
.\mvnw.cmd test
```
