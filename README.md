# ⛽ Gas Station Management System

A Spring Boot REST API application demonstrating database relationships, pagination, sorting, and Rwanda's 5-level administrative hierarchy management.

---

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Technology Stack](#technology-stack)
- [Database Design](#database-design)
- [Setup Instructions](#setup-instructions)
- [Key Features Explained](#key-features-explained)
- [API Endpoints](#api-endpoints)
- [Testing Guide](#testing-guide)

---

##  Project Overview

This system manages:
- **Locations**: Rwanda's hierarchy (Province → District → Sector → Cell → Village)
- **Customers**: Users registered to villages
- **Loyalty Cards**: One card per customer
- **Fuel Discounts**: Promotional programs
- **Fuel Transactions**: Purchase records

---

## 🛠️ Technology Stack

- **Java 21**
- **Spring Boot 3.2.3**
- **Spring Data JPA**
- **PostgreSQL**
- **Maven**

---

## 🗄️ Database Design

### Entity Relationship Diagram

```
Location (Self-Referencing: parent_id)
    ↓ One-to-Many
Customer
    ├── One-to-One → LoyaltyCard
    ├── One-to-Many → FuelTransaction
    └── Many-to-Many → FuelDiscount (via customer_discounts table)
```

### Five Tables

1. **locations** - Self-referencing hierarchy
2. **customers** - Customer information
3. **loyalty_cards** - One card per customer
4. **fuel_discounts** - Discount programs
5. **fuel_transactions** - Purchase records
6. **customer_discounts** - Join table (Many-to-Many)

---



<img width="610" height="430" alt="Image" src="https://github.com/user-attachments/assets/bca37286-d1c2-4215-9476-76b551988407" />





## 🚀 Setup Instructions

### 1. Prerequisites
- Java 21
- PostgreSQL
- Maven

### 2. Database Setup
```sql
CREATE DATABASE gas_station_db;
```

### 3. Configure Application
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gas_station_db
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### 4. Run Application
```bash
mvn spring-boot:run
```

Application runs on: `http://localhost:8080`

---

## 🔑 Key Features Explained

### 1️⃣ Saving Location (Self-Referencing Relationship)

**How it works:**
- Parent locations (Provinces) have no parent: `parent = null`
- Child locations reference their parent using `parent_id` foreign key
- Creates hierarchy: Province → District → Sector → Cell → Village

**Code Implementation:**
```java
@ManyToOne
@JoinColumn(name = "parent_id")
private Location parent;

@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
private List<Location> subLocations;
```

**Example:**
```bash
# Create Province (no parent)
POST /api/locations/parent
{
  "code": "KG",
  "name": "Kigali",
  "locationType": "PROVINCE"
}

# Create District (with parent)
POST /api/locations/child?parentId={provinceId}
{
  "code": "KG-GAS",
  "name": "Gasabo",
  "locationType": "DISTRICT"
}
```

---

### 2️⃣ Pagination and Sorting

**How it works:**
- **Pagination**: Loads data in pages (e.g., 10 items per page) instead of all at once
- **Sorting**: Orders data by any field (name, date, etc.) in ascending/descending order
- **Performance**: Reduces memory usage and speeds up queries

**Code Implementation:**
```java
Sort sort = direction.equalsIgnoreCase("desc") 
    ? Sort.by(sortBy).descending() 
    : Sort.by(sortBy).ascending();

Pageable pageable = PageRequest.of(page, size, sort);
return customerRepository.findAll(pageable);
```

**Example:**
```bash
GET /api/customers?page=0&size=10&sortBy=fullName&direction=asc
```

**Response includes:**
- `content`: List of items
- `totalElements`: Total count
- `totalPages`: Number of pages
- `number`: Current page

---

### 3️⃣ One-to-One Relationship (Customer ↔ LoyaltyCard)

**How it works:**
- Each customer has exactly ONE loyalty card
- `customer_id` in loyalty_cards table with `unique=true` constraint
- Card is automatically created when customer registers

**Code Implementation:**
```java
// In LoyaltyCard.java (Owning side)
@OneToOne
@JoinColumn(name = "customer_id", unique = true)
private Customer customer;

// In Customer.java (Inverse side)
@OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
private LoyaltyCard loyaltyCard;
```

**Example:**
```bash
# Card created automatically with customer
POST /api/customers?villageId={villageId}
{
  "fullName": "John Doe",
  "email": "john@example.com"
}
```

---

### 4️⃣ One-to-Many Relationship (Customer → FuelTransaction)

**How it works:**
- One customer can have MANY transactions
- Each transaction belongs to ONE customer
- `customer_id` foreign key in fuel_transactions table

**Code Implementation:**
```java
// In FuelTransaction.java (Many side - owns relationship)
@ManyToOne
@JoinColumn(name = "customer_id")
private Customer customer;

// In Customer.java (One side)
@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
private List<FuelTransaction> fuelTransactions;
```

**Example:**
```bash
POST /api/transactions?customerId=1
{
  "fuelType": "DIESEL",
  "quantityLiters": 50.0,
  "pricePerLiter": 1200.0,
  "totalAmount": 60000.0
}
```

---

### 5️⃣ Many-to-Many Relationship (Customer ↔ FuelDiscount)

**How it works:**
- Many customers can have many discounts
- Uses join table `customer_discounts` with two foreign keys
- Join table automatically managed by JPA

**Code Implementation:**
```java
// In Customer.java (Owning side)
@ManyToMany
@JoinTable(
    name = "customer_discounts",
    joinColumns = @JoinColumn(name = "customer_id"),
    inverseJoinColumns = @JoinColumn(name = "discount_id")
)
private List<FuelDiscount> fuelDiscounts;

// In FuelDiscount.java (Inverse side)
@ManyToMany(mappedBy = "fuelDiscounts")
private List<Customer> customers;
```

**Join Table Structure:**
```
customer_discounts
├── customer_id (FK → customers.id)
└── discount_id (FK → fuel_discounts.id)
```

**Example:**
```bash
# Assign discount to customer
POST /api/discounts/1/assign?customerId=1

# Remove discount from customer
POST /api/discounts/1/remove?customerId=1
```

---

### 6️⃣ existsBy() Method

**How it works:**
- Checks if a record exists without loading it
- Returns `true` or `false`
- More efficient than `findBy()` for validation

**Code Implementation:**
```java
// In Repository
boolean existsByEmail(String email);
boolean existsByCode(String code);

// In Service
if (customerRepository.existsByEmail(email)) {
    throw new RuntimeException("Email already exists");
}
```

**Example:**
```bash
GET /api/customers/exists/email/john@example.com
Response: {"exists": true}
```

---

### 7️⃣ Retrieve Customers by Province (Code OR Name)

**How it works:**
- Search by location code (e.g., "KG") OR name (e.g., "Kigali")
- Works at any level: Province, District, Sector, Cell, or Village
- Recursively finds all customers in sub-locations

**Code Implementation:**
```java
public List<Customer> getCustomersByLocation(String locationCodeOrName) {
    // Try code first, then name
    Location location = locationRepository.findByCode(locationCodeOrName)
        .orElseGet(() -> locationRepository.findByName(locationCodeOrName)
            .orElseThrow(() -> new RuntimeException("Location not found")));
    
    // If village, return customers directly
    if (location.getLocationType() == LocationType.VILLAGE) {
        return location.getCustomers();
    }
    
    // If higher level, collect all customers from sub-locations
    return getAllCustomersUnderLocation(location);
}
```

**Example:**
```bash
# By province code
GET /api/customers/by-location?locationCodeOrName=KG

# By province name
GET /api/customers/by-location?locationCodeOrName=Kigali

# By district name
GET /api/customers/by-location?locationCodeOrName=Gasabo
```

---

## 📚 API Endpoints

### Locations

```bash
# Create parent location (Province)
POST /api/locations/parent
{
  "code": "KG",
  "name": "Kigali",
  "locationType": "PROVINCE"
}

# Create child location
POST /api/locations/child?parentId={uuid}
{
  "code": "KG-GAS",
  "name": "Gasabo",
  "locationType": "DISTRICT"
}

# Get all locations (paginated)
GET /api/locations?page=0&size=10&sortBy=name&direction=asc

# Get by code
GET /api/locations/code/KG

# Get by type
GET /api/locations/type/PROVINCE

# Get children
GET /api/locations/{parentId}/children

# Search
GET /api/locations/search?keyword=gasabo

# Check if exists
GET /api/locations/exists/code/KG
```

### Customers

```bash
# Register customer
POST /api/customers?villageId={uuid}
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "phone": "+250788123456"
}

# Get all (paginated & sorted)
GET /api/customers?page=0&size=10&sortBy=fullName&direction=asc

# Get by ID
GET /api/customers/1

# Get by email
GET /api/customers/email/john@example.com

# Get by location (code or name)
GET /api/customers/by-location?locationCodeOrName=Kigali

# Check if email exists
GET /api/customers/exists/email/john@example.com

# Update
PUT /api/customers/1
{
  "fullName": "John Updated",
  "email": "john.updated@example.com"
}

# Delete
DELETE /api/customers/1
```

### Loyalty Cards

```bash
# Create card for customer
POST /api/loyalty-cards?customerId=1

# Get all (paginated)
GET /api/loyalty-cards?page=0&size=10

# Get by customer ID
GET /api/loyalty-cards/customer/1

# Update tier
PUT /api/loyalty-cards/1/tier?tier=GOLD

# Add points
PUT /api/loyalty-cards/1/points?points=100

# Check if exists
GET /api/loyalty-cards/exists/number/LC-12345678
```

### Fuel Discounts

```bash
# Create discount
POST /api/discounts
{
  "discountCode": "SUMMER2024",
  "discountName": "Summer Sale",
  "discountPercentage": 15.0,
  "isActive": true
}

# Get all (paginated)
GET /api/discounts?page=0&size=10

# Assign to customer (Many-to-Many)
POST /api/discounts/1/assign?customerId=1

# Remove from customer
POST /api/discounts/1/remove?customerId=1

# Get customer's discounts
GET /api/discounts/customer/1

# Check if exists
GET /api/discounts/exists/code/SUMMER2024
```

### Fuel Transactions

```bash
# Create transaction
POST /api/transactions?customerId=1
{
  "fuelType": "DIESEL",
  "quantityLiters": 50.0,
  "pricePerLiter": 1200.0,
  "totalAmount": 60000.0
}

# Get all (paginated)
GET /api/transactions?page=0&size=10&sortBy=transactionDate&direction=desc

# Get by customer
GET /api/transactions/customer/1?page=0&size=10

# Get by fuel type
GET /api/transactions/fuel-type/DIESEL?page=0&size=10

# Get total sales
GET /api/transactions/reports/total-sales

# Check if exists
GET /api/transactions/exists/code/TXN-12345678
```

---

## 🧪 Testing Guide

### Step-by-Step Test Flow

**1. Create Location Hierarchy**
```bash
# Province
POST /api/locations/parent
{"code": "KG", "name": "Kigali", "locationType": "PROVINCE"}

# District (use province ID from response)
POST /api/locations/child?parentId={provinceId}
{"code": "KG-GAS", "name": "Gasabo", "locationType": "DISTRICT"}

# Sector
POST /api/locations/child?parentId={districtId}
{"code": "KG-GAS-REM", "name": "Remera", "locationType": "SECTOR"}

# Cell
POST /api/locations/child?parentId={sectorId}
{"code": "KG-GAS-REM-GIS", "name": "Gisimenti", "locationType": "CELL"}

# Village
POST /api/locations/child?parentId={cellId}
{"code": "KG-GAS-REM-GIS-V1", "name": "Kigali Village", "locationType": "VILLAGE"}
```

**2. Register Customer**
```bash
POST /api/customers?villageId={villageId}
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "phone": "+250788123456"
}
# Loyalty card is automatically created!
```

**3. Create and Assign Discount**
```bash
# Create discount
POST /api/discounts
{
  "discountCode": "SUMMER2024",
  "discountName": "Summer Sale",
  "discountPercentage": 15.0,
  "isActive": true
}

# Assign to customer (Many-to-Many)
POST /api/discounts/1/assign?customerId=1
```

**4. Create Transaction**
```bash
POST /api/transactions?customerId=1
{
  "fuelType": "DIESEL",
  "quantityLiters": 50.0,
  "pricePerLiter": 1200.0,
  "totalAmount": 60000.0
}
```

**5. Test Province Search**
```bash
# By code
GET /api/customers/by-location?locationCodeOrName=KG

# By name
GET /api/customers/by-location?locationCodeOrName=Kigali

# Should return all customers in Kigali province
```

**6. Test Pagination**
```bash
GET /api/customers?page=0&size=5&sortBy=fullName&direction=asc
```

**7. Test existsBy()**
```bash
GET /api/customers/exists/email/john@example.com
# Response: {"exists": true}
```

---

## 📝 Project Structure

```
src/main/java/com/example/Gas_Station_Management_System/
├── controller/       # REST endpoints
├── entity/          # Database entities
├── repository/      # Data access layer
└── service/         # Business logic
```

---

## 🎓 What This Project Demonstrates

✅ **5 Entity Tables** with relationships  
✅ **Self-Referencing** hierarchy (Location)  
✅ **One-to-One** (Customer ↔ LoyaltyCard)  
✅ **One-to-Many** (Customer → FuelTransaction)  
✅ **Many-to-Many** (Customer ↔ FuelDiscount)  
✅ **Pagination & Sorting** on all list endpoints  
✅ **existsBy()** methods for validation  
✅ **Province Search** by code OR name with recursive traversal  

---

**Made with Spring Boot 3.2.3 | Java 21 | PostgreSQL**
