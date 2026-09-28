![Lexicon Logo](https://lexicongruppen.se/media/wi5hphtd/lexicon-logo.svg)

# E-commerce Platform JPA Workshop

A Spring Boot and Spring Data JPA workshop project covering the customer,
catalog, promotion, and ordering domain. Parts 1–3 are implemented, including
the Part 3 REST API and both optional category and promotion services.

## Current implementation

- JPA entities and relationships for customers, addresses, user profiles,
  categories, products, product images, promotions, orders, and order items.
- Spring Data repositories with derived queries, JPQL queries, and eager loading
  for order items on status-based order lookups.
- Idempotent startup seeding for three categories and their sample products.
- Immutable request and response DTO records with Bean Validation constraints.
- `CustomerMapper`, `ProductMapper`, and `OrderMapper` Spring components.
- `CustomerService`, `ProductService`, and `OrderService`, each using an
  interface and implementation.
- `CategoryService` and `PromotionService` for optional category management and
  promotion selection/discount calculations.
- Duplicate-email and resource-not-found exceptions used by the services.
- REST controllers for customers, products, orders, categories, and promotions.
  Create operations return `201 Created` with a `Location` header.
- `ApiExceptionHandler` returns RFC 9457-style `ProblemDetail` responses:
  `400 Bad Request` for invalid input, `404 Not Found` for missing resources,
  and `409 Conflict` for duplicates.
- Transactional customer registration and updates, product/category/promotion
  creation, and order placement. Orders use the highest-percentage promotion
  active for each product and persist the resulting price at purchase time.
- H2 integration coverage that forces an item insert to fail and verifies the
  entire order transaction rolls back.
- MockMvc integration coverage for the REST workflows, validation, duplicate
  resources, missing resources, and the best-active-promotion rule.

## Workshop status and scope

- Part 1: customer and address persistence, including optional user profiles.
- Part 2: domain mappings, repository queries, and seed data.
- Part 3, Tasks 1–4: DTOs, mappers, services, transaction rollback coverage,
  custom exceptions, centralized HTTP error mapping, and REST controllers are
  implemented.
- Both Part 3 optional services are implemented: categories can be created and
  listed; promotions can be created, listed while active, and evaluated for a
  product. If several promotions apply, the greatest discount percentage wins.
- `CustomerRequest` validates a password, but the current `Customer` entity has
  no password field or authentication feature. The mapper intentionally does
  not persist it; this workshop API has no authentication or authorization.

## REST API

The API is available under `/api` when the application is running. Request
bodies for create/update operations are validated; invalid values return 400,
duplicate resources return 409, and unknown IDs return 404.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/customers` | Register a customer |
| `GET` | `/api/customers/{id}` | Get a customer |
| `PUT` | `/api/customers/{id}` | Update a customer |
| `POST` | `/api/products` | Create a product |
| `GET` | `/api/products` | List products; optional `?name=...` searches by name |
| `GET` | `/api/products/{id}` | Get a product |
| `POST` | `/api/orders` | Place an order |
| `GET` | `/api/orders/{id}` | Get an order |
| `POST` | `/api/categories` | Create a category |
| `GET` | `/api/categories` | List categories |
| `GET` | `/api/categories/{id}` | Get a category |
| `POST` | `/api/promotions` | Create a percentage promotion for product IDs |
| `GET` | `/api/promotions/active` | List promotions active today |
| `GET` | `/api/promotions/{id}` | Get a promotion |
| `GET` | `/api/promotions/products/{productId}/discount` | Preview the best active discount for a product |

Promotions use inclusive start/end dates, with a missing end date meaning no
expiry. The discount preview and order placement both use the highest active
percentage for the product. For example, create a promotion with a JSON body
like `{"code":"SUMMER15","startDate":"2026-09-01","endDate":"2026-09-30","discountPercentage":15,"productIds":[1]}`.

## Run locally

### Requirements

- JDK 26 (the Maven compiler targets Java 26).
- Apache Maven installed and available as `mvn` in PowerShell.
- MySQL is only needed when using the optional `mysql` profile.

The default Spring profile is `h2`. It uses an in-memory H2 database, creates
the schema on startup, and seeds sample catalog data. Start the app from the
project root:

```powershell
mvn spring-boot:run
```

On this Windows/JDK 26 setup, if Maven or Spring Boot encounters a temporary
directory or compiler process issue, create a project-local temporary folder
and use the forked compiler command:

```powershell
New-Item -ItemType Directory -Force target/app-tmp
mvn "-Dmaven.compiler.fork=true" "-Dspring-boot.run.jvmArguments=-Djava.io.tmpdir=target/app-tmp" spring-boot:run
```

The H2 console is available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
while the app is running. Use JDBC URL `jdbc:h2:mem:ecommerce`, username `sa`,
and a blank password.

### Optional MySQL profile

Create the `ecommerce` database in MySQL, set connection details in the current
PowerShell session, then activate the `mysql` profile:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/ecommerce"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-local-password"
$env:SPRING_PROFILES_ACTIVE = "mysql"
mvn "-Dmaven.compiler.fork=true" spring-boot:run
```

The MySQL profile uses `spring.jpa.hibernate.ddl-auto=update`; use it for local
workshop development, not as a production schema migration strategy.

## Run tests

Run the full test suite:

```powershell
mvn "-Dmaven.compiler.fork=true" test
```

Run a clean build and test suite:

```powershell
mvn "-Dmaven.compiler.fork=true" clean test
```

Run focused service, transaction rollback, or REST integration tests:

```powershell
mvn "-Dmaven.compiler.fork=true" "-Dtest=ServiceLayerTest" test
mvn "-Dmaven.compiler.fork=true" "-Dtest=OrderServiceTransactionTest" test
mvn "-Dmaven.compiler.fork=true" "-Dtest=CommerceApiIntegrationTest" test
```

Tests use the `test` profile and an H2 database configured with schema
creation/drop. The suite covers entity mappings, repositories and queries,
DTO validation, mapper behavior, service rules, seeding, HTTP workflows, and
transaction rollback. `.mvn/maven.config` points Maven's dependency cache to
`.mvn/repository`; the compiler fork option is included above for the current
Windows/JDK 26 environment.

## Project structure

```text
src/main/java/se/lexicon/ecommerce/
|-- EcommerceApplication.java          Spring Boot entry point
|-- controller/                        REST controllers under /api
|-- domain/                            JPA entities and OrderStatus enum
|-- dto/                               Request and response records
|-- exception/                         Service exceptions and HTTP advice
|-- mapper/                            Entity and DTO mapping components
|-- repository/                        Spring Data JPA repositories
|-- seed/                              Startup catalog seeder
`-- service/                            Service interfaces and implementations

src/main/resources/
|-- application.yml                    Default profile and shared settings
|-- application-h2.yml                 H2 development configuration
`-- application-mysql.yml              Optional MySQL configuration

src/test/java/se/lexicon/ecommerce/
|-- domain/                            Entity mapping tests
|-- controller/                        REST API integration tests
|-- dto/                               DTO validation tests
|-- exception/                         HTTP exception-mapping tests
|-- mapper/                            Mapper tests
|-- repository/                        Repository query tests
|-- seed/                              Seeder tests
`-- service/                            Service and transaction tests
```
