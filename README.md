![Lexicon Logo](https://lexicongruppen.se/media/wi5hphtd/lexicon-logo.svg)

# E-commerce Platform JPA Workshop

A Spring Boot and Spring Data JPA workshop project covering the customer,
catalog, promotion, and ordering domain. Parts 1 and 2 are implemented. Part 3
currently includes its DTO and mapper layer, service layer, and transaction
rollback coverage.

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
- Duplicate-email and resource-not-found exceptions used by the services.
- `ApiExceptionHandler` maps those exceptions to HTTP 409 Conflict and 404 Not
  Found `ProblemDetail` responses for Spring MVC controllers.
- Transactional customer registration and updates, product creation, and order
  placement. Order items retain the product's current price at purchase time.
- H2 integration coverage that forces an item insert to fail and verifies the
  entire order transaction rolls back.

## Workshop status and current limits

- Part 1: customer and address persistence, including optional user profiles.
- Part 2: domain mappings, repository queries, and seed data.
- Part 3, Tasks 1–3: DTOs, mappers, services, and order transaction rollback
  coverage are implemented.
- Part 3, Task 4: custom service exceptions and centralized HTTP error mapping
  are implemented.
- Optional category and promotion services are not implemented. Promotions are
  mapped in the domain and repository, but are not applied during order
  placement.
- There are no REST controllers yet. Running the application starts the Spring
  Boot app and seeds the database; it does not expose customer, product, or
  order API endpoints.
- `CustomerRequest` validates a password, but the current `Customer` entity has
  no password field or authentication feature. The mapper intentionally does
  not persist it.
- `CategoryResponse` is available for the optional category service and is not
  currently used by application code.

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

Run only the service unit tests or the transaction rollback integration test:

```powershell
mvn "-Dmaven.compiler.fork=true" "-Dtest=ServiceLayerTest" test
mvn "-Dmaven.compiler.fork=true" "-Dtest=OrderServiceTransactionTest" test
```

Tests use the `test` profile and an H2 database configured with schema
creation/drop. The suite covers entity mappings, repositories and queries,
DTO validation, mapper behavior, service rules, seeding, and transaction
rollback. `.mvn/maven.config` points Maven's dependency cache to
`.mvn/repository`; the compiler fork option is included above for the current
Windows/JDK 26 environment.

## Project structure

```text
src/main/java/se/lexicon/ecommerce/
|-- EcommerceApplication.java          Spring Boot entry point
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
|-- dto/                               DTO validation tests
|-- mapper/                            Mapper tests
|-- repository/                        Repository query tests
|-- seed/                              Seeder tests
`-- service/                            Service and transaction tests
```
