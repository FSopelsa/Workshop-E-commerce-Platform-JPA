    ![Lexicon Logo](https://lexicongruppen.se/media/wi5hphtd/lexicon-logo.svg)

# Workshop: E-commerce Platform JPA (Part 2)

## Objective

Extend the E-commerce system to handle Catalog management, transactions, and many-to-many promotional relationships.

## Project Setup & Verification

This section continues from **Part 1**. Before coding the entities and relationships:

1. **Create a new branch for Part 2**
2. **Run the application** – Verify that the application starts without errors and that the database connection is successful.

## Domain Model: Catalog & Transactional Entities

In Part 2, the product catalog and the ordering process are introduced:

- **Category**: Product grouping (Electronics, Books, etc.).
- **Product**: Items available for sale.
- **Promotion**: Discounts that can apply to many products (with an active date range).
- **Order**: A completed purchase placed by a customer.
- **OrderItem**: A specific product in an order with price and quantity.
- **OrderStatus**: The state of an order (e.g., `CREATED`, `PAID`, `SHIPPED`, `CANCELLED`).

> **Note**: `Customer` is defined in Part 1 and is reused here. Each `Order` belongs to exactly one `Customer`.

## Class Diagram (UML)

This diagram illustrates the Java class structure and relationship cardinalities.

```mermaid
classDiagram
    direction LR

    class Customer {
        Long id
        String firstName
        String lastName
        String email
        Instant createdAt
        Address address
        UserProfile profile
    }

    class Address {
        Long id
        String street
        String city
        String zipCode
    }

    class UserProfile {
        Long id
        String nickname
        String phoneNumber
        String bio
    }

    class Category {
        Long id
        String name
    }

    class Product {
        Long id
        String name
        List~String~ imageUrls
        BigDecimal price
        Category category
        Set~Promotion~ promotions
    }

    class Promotion {
        Long id
        String code
        LocalDate startDate
        LocalDate endDate
    }

    class Order {
        Long id
        Instant orderDate
        OrderStatus status
        Customer customer
        List~OrderItem~ items
    }

    class OrderItem {
        Long id
        Integer quantity
        BigDecimal priceAtPurchase
        Order order
        Product product
    }

    class OrderStatus {
        <<enumeration>>
        CREATED
        PAID
        SHIPPED
        CANCELLED
    }

    Customer "1" --> "1" Address: address_id (mandatory)
    Customer "1" --> "0..1" UserProfile: profile_id (optional)
    Category "1" --> "0..*" Product: products
    Product "0..*" --> "1" Category: category_id
    Product "0..*" -- "0..*" Promotion: products_promotions
    Customer "1" --> "0..*" Order: customer_id
    Order "1" --> "1..*" OrderItem: items
    Order "0..*" --> "1" OrderStatus: status
    OrderItem "0..*" --> "1" Product: product_id
```

## Entity Diagram

```mermaid
erDiagram
    ADDRESSES {
        BIGINT id PK
        VARCHAR street
        VARCHAR city
        VARCHAR zip_code
    }

    USER_PROFILES {
        BIGINT id PK
        VARCHAR nickname
        VARCHAR phone_number
        VARCHAR bio
    }

    CUSTOMERS {
        BIGINT id PK
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR email UK
        TIMESTAMP created_at
        BIGINT address_id FK
        BIGINT profile_id FK
    }

    CATEGORIES {
        BIGINT id PK
        VARCHAR name
    }

    PRODUCTS {
        BIGINT id PK
        VARCHAR name
        DECIMAL price
        BIGINT category_id FK
    }

    PRODUCT_IMAGES {
        BIGINT product_id FK
        VARCHAR image_url
    }

    PROMOTIONS {
        BIGINT id PK
        VARCHAR code UK
        DATE start_date
        DATE end_date
    }

    PRODUCTS_PROMOTIONS {
        BIGINT product_id FK
        BIGINT promotion_id FK
    }

    ORDERS {
        BIGINT id PK
        TIMESTAMP order_date
        VARCHAR status
        BIGINT customer_id FK
    }

    ORDER_ITEMS {
        BIGINT id PK
        INT quantity
        DECIMAL price_at_purchase
        BIGINT order_id FK
        BIGINT product_id FK
    }

%% One-to-One
    ADDRESSES ||--|| CUSTOMERS: address_id
    USER_PROFILES ||--o| CUSTOMERS: profile_id
%% Category/Product (1:M)
    CATEGORIES ||--o{ PRODUCTS: category_id
%% Product/Promotion (M:N)
    PRODUCTS ||--o{ PRODUCT_IMAGES: product_id
    PRODUCTS ||--o{ PRODUCTS_PROMOTIONS: product_id
    PROMOTIONS ||--o{ PRODUCTS_PROMOTIONS: promotion_id
%% Customer/Order (1:M)
    CUSTOMERS ||--o{ ORDERS: customer_id
%% Order/OrderItem (1:M)
    ORDERS ||--o{ ORDER_ITEMS: order_id
%% OrderItem/Product (M:1)
    PRODUCTS ||--o{ ORDER_ITEMS: product_id
```

## ORM Mapping

### 1. Product & Category (Many-to-One)

- **Task**: Implement a relationship where many products can belong to one category.
- **Requirements**:
    - Use the appropriate JPA annotation to link `Product` to `Category`. Specify the foreign key column as
      `category_id`.
    - **Fetch Type**: Explicitly configure the fetch strategy for this relationship (consider the default for
      `@ManyToOne`).
- **Optional**: Configure bidirectional access in the `Category` class.

---

### 2. Order & OrderItem (One-to-Many)

- **Task**: Implement a bidirectional relationship between an `Order` and its `OrderItem`s.
- **Requirements**:
    - Identify which side should hold the foreign key (`order_id`) and mark it as the owner side.
    - Use the `mappedBy` attribute correctly on the inverse side.
    - **Cascading**: Configure cascading so that saving or deleting an `Order` affects its items, and ensure orphan
      removal is enabled.
    - **Fetch Type**: Configure the fetch type for the collection of items (consider the default for `@OneToMany`).
- **Business Rule**:
    - An `Order` must contain at least one `OrderItem` before it is saved.

---

### 3. Order & Customer (Many-to-One)

- **Task**: Link each `Order` to exactly one `Customer` (from Part 1).
- **Requirements**:
    - Map a `@ManyToOne` from `Order` to `Customer` with a foreign key column named `customer_id`.
    - **Fetch Type**: Explicitly configure the fetch strategy (consider the default for `@ManyToOne`).

---

### 4. Order Status (Enum)

- **Task**: Add an `OrderStatus` field to `Order`.
- **Requirements**:
    - Store the enum value as a readable string in the database (not an ordinal).

---

### 5. Product & Promotion (Many-to-Many)

- **Task**: Implement a relationship where a product can have multiple active promotions and a promotion can apply to
  multiple products.
- **Requirements**:
    - Use a join table (e.g., `products_promotions`) to manage this relationship with join columns `product_id` and
      `promotion_id`.
    - In this workshop, **Product is the owner side** and defines the join table.
    - If making it bidirectional, ensure the other side ensures the other side uses the `mappedBy` attribute.
    - **Fetch Type**: Specify the fetch type for the many-to-many relationship.
    - **Cascading**: Avoid `CascadeType.ALL` for this relationship.

---

### 6. Reference Relationship (OrderItem → Product)

- **Task**: Link `OrderItem` to `Product`.
- **Requirements**:
    - Ensure each `OrderItem` references exactly one `Product`.
    - **Fetch Type**: Configure the fetch type for this relationship (referencing the product).
- **Note**:
    - Consider why `priceAtPurchase` is stored in `OrderItem` instead of just relying on the current price in the
      `Product` entity.

---

## Repository Layer Requirements

Create repository interfaces for the entities listed below.

You are expected to use Spring Data JPA query method naming and, where appropriate, custom queries.

> **Note**: For `OrderRepository`, at least one query must demonstrate a strategy to avoid the **N+1 problem** when loading order items.

---

### 1. CategoryRepository

#### Required Queries
- Find a category by name (case-insensitive).
- Check if a category exists by name.

#### Optional / Advanced Queries
- Find categories whose name contains a given keyword.
- Count how many categories exist.

---

### 2. ProductRepository

#### Required Queries
- Find products by their category name.
- Find products within a specific price range.

#### Optional / Advanced Queries
- Find products whose name contains a given keyword.
- Find products cheaper than a given price.
- Find products ordered by price (ascending or descending).
- Count how many products exist in a specific category.
- Find products by category ID.

---

### 3. OrderRepository

#### Required Queries
- Find all orders belonging to a specific customer ID.
- Find orders by status and use a strategy to avoid the **N+1 problem** (loading order items in the same query).

#### Optional / Advanced Queries
- Find orders created after a specific date.
- Find orders created between two dates.
- Find orders that contain a specific product.
- Count orders by status.
- Find orders by customer ID and status.

**Clarification (N+1)**  
For the “find by status” query, use one of the following approaches:
- `@EntityGraph` to load `items` in the same query, **or**
- `JOIN FETCH` in JPQL.

---

### 4. OrderItemRepository (Optional)

> This repository is optional because `OrderItem` is typically managed through `Order` using cascading and orphan removal.

#### Optional / Advanced Queries
- Find all order items belonging to a specific order ID.
- Find all order items for a specific product ID.
- Find order items where quantity is greater than a given value.

---

### 5. PromotionRepository

#### Required Queries
- Find promotions that are active on a given date.

#### Optional / Advanced Queries
- Find promotions by code.
- Find promotions starting after a given date.
- Find promotions ending before a given date.
- Find promotions that have no end date.
- Find promotions active today.

---

## Extra Task: Data Seeding

When the application runs, create a mechanism to automatically insert initial test data into the database.

### Goals
- Automatically insert a list of categories.
- Automatically insert a list of products linked to those categories.
- Ensure that the data is only inserted once (avoid duplicates).

### Requirements
- Categories must be created before products.
- Each product must reference an existing category.
- The application should still start successfully if the data already exists.

---

### Learning Goals
- Practice navigating nested properties in query methods.
- Understand when derived queries are sufficient and when custom queries are needed.
- Explore strategies for avoiding the N+1 problem (EntityGraph vs Join Fetch).
- Understand when it makes sense to create repositories for child entities (like `OrderItem`) vs managing them through aggregates (like `Order`).

---

## Submission Checklist

- [x] **Git Branch**: Part 2 work is on `feature/jpa-part2`.
- [x] **Entities & Enums**: `Category`, `Product`, `Promotion`, `Order`, `OrderItem`, and `OrderStatus` are implemented.
- [x] **Relationships**: The required Product/Category, Order/OrderItem, Order/Customer, and Product/Promotion mappings are implemented with the specified ownership, fetch, cascade, and orphan-removal rules.
- [x] **Repositories**: The required and optional repository queries are implemented, including both product price sort directions, `@EntityGraph` order loading, category counts, and active-today promotion lookup.
- [x] **Extra Task**: `CatalogDataSeeder.java` implements idempotent category and product seeding, with categories created before products.
- [x] **Verification**: The application starts with H2 and the Maven suite passes with 13 tests, covering schema generation, mappings, queries, both N+1-safe status-loading methods, and seeding.
- [ ] **Review follow-up commit**: The current documentation, sorting, and exact `findByStatus` test improvements are intentionally uncommitted for manual review.
- [ ] **Review follow-up push**: The review follow-up changes still need to be committed and pushed manually. The existing Part 2 implementation is already pushed, and nothing from Part 2 has been merged into `main`.

### Current Git boundary

Before this review follow-up, `feature/jpa-part2` and its remote-tracking branch pointed to `5f6c3d5`. The current local edits are in `ProductRepository.java`, `Part2RepositoryQueryTest.java`, `README.md`, and this instruction file; they are intentionally not committed so they can be reviewed and committed manually.

The Part 2 work remains on `feature/jpa-part2`; `main` still points to the Part 1 line of history.

---
