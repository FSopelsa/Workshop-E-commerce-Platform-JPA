# E-commerce Platform JPA Workshop

This is my completed Spring Boot Data JPA workshop project for the e-commerce
platform assignment. The project contains the Part 1 customer model and the
Part 2 catalog, promotion, and ordering model.

I have implemented the required JPA entities, relationships, repositories,
query methods, startup seed data, and verification tests. The application uses
H2 by default for local development and testing, with a separate MySQL profile
available when needed.

## Implemented Features

The project includes the following domain areas:

- Customer persistence from Part 1, including mandatory address mapping and
  optional user profile mapping.
- Product catalog management with categories, products, product images, and
  promotions.
- Order handling with orders, order items, purchase-time prices, quantities,
  customers, and order status.
- Repository queries for categories, products, orders, order items, and
  promotions.
- N+1-safe order loading using `@EntityGraph` when querying orders by status.
- Idempotent startup seeding for the initial catalog data.
- Automated tests for mappings, repositories, query behavior, and data seeding.

## Domain Model

The completed model contains these main entities:

- `Customer` owns a mandatory one-to-one `Address`.
- `Customer` also has an optional bidirectional one-to-one `UserProfile`.
- `Category` groups products and has bidirectional access to its products.
- `Product` belongs to one required `Category`.
- `Product` can have many `Promotion` entries through the `products_promotions`
  join table.
- `Promotion` can apply to many products and can be queried by active date.
- `Order` belongs to one required `Customer`.
- `Order` owns one or more `OrderItem` entries with cascade and orphan removal.
- `OrderItem` references one required `Product` and stores `priceAtPurchase`.
- `OrderStatus` is stored as a readable string using `EnumType.STRING`.

## Repository Coverage

I implemented the required repository layer with both derived Spring Data JPA
queries and JPQL where it fits the assignment.

Implemented repository behavior includes:

- Category lookup by name, existence check, keyword search, and category count.
- Product lookup by category name, price range, keyword, max price, category ID,
  sorted price in both directions, and product count per category.
- Order lookup by customer, status, date range, contained product, status count,
  and customer/status combination.
- Order item lookup by order ID, product ID, and minimum quantity.
- Promotion lookup by active date, code, start date, end date, no end date, and
  active today.

## Data Seeding

`CatalogDataSeeder` inserts initial catalog data when the application starts.
It creates categories before products, links each product to an existing
category, and checks for existing data so repeated application starts do not
create duplicates.

## Project Structure

```text
src/main/java/se/lexicon/ecommerce/
|-- EcommerceApplication.java          Spring Boot entry point
|-- domain/                            JPA entities and OrderStatus enum
|-- repository/                        Spring Data JPA repositories
`-- seed/                              Startup catalog data seeder

src/main/resources/
|-- application.yml                    Default H2 configuration
`-- application-mysql.yml              Optional MySQL profile

src/test/
|-- java/se/lexicon/ecommerce/domain/  Mapping tests
|-- java/se/lexicon/ecommerce/repository/
|                                      Repository query tests
`-- java/se/lexicon/ecommerce/seed/    Seeder tests
```

## Verification

I verified the project with the Maven test suite. The tests cover schema
generation, relationship mappings, repository queries, readable enum storage,
N+1-safe order loading, and idempotent catalog seeding.

Run all tests:

```powershell
mvn clean test
```

Expected result:

```text
BUILD SUCCESS
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
```

On my current Windows/JDK 26 environment, this equivalent command avoids a Java
ZipFS cache-access issue:

```powershell
mvn "-Dmaven.repo.local=target\maven-repository" "-Dmaven.compiler.fork=true" test
```

## Running The Application

Run the application with the default H2 profile:

```powershell
mvn spring-boot:run
```

The application starts with an in-memory H2 database and seeds the catalog on
startup. The H2 console is available while the application is running:

```text
http://localhost:8080/h2-console
```

To run with MySQL instead, use the `mysql` profile and provide `DB_URL`,
`DB_USERNAME`, and `DB_PASSWORD`.

On my current Windows/JDK 26 environment, this startup command is the most
reliable:

```powershell
mvn "-Dmaven.repo.local=target\maven-repository" "-Dmaven.compiler.fork=true" "-Dmaven.test.skip=true" spring-boot:run "-Dspring-boot.run.jvmArguments=-Djava.io.tmpdir=target/app-tmp"
```

## Examination Status

The required Part 2 ORM mapping tasks, repository tasks, optional repository
tasks, and extra data seeding task are implemented. The project has been
verified with the H2 startup configuration and the complete 13-test Maven
suite.
