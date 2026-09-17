# E-commerce Platform JPA Workshop

This branch continues the Spring Boot/JPA workshop with the Part 2 catalog and
ordering model. It keeps the Part 1 customer mappings and adds categories,
products, promotions, orders, order items, repositories, and startup seed data.

The customer owns a mandatory, unidirectional one-to-one address relationship.
Saving or deleting a customer cascades to its address, and orphan removal is
enabled. `createdAt` is assigned automatically just before a customer is first
persisted.

The customer also owns an optional bidirectional one-to-one profile relationship;
the profile exposes the inverse side with `mappedBy`.

Part 2 adds the following mappings:

- `Product` has one required, lazy-loaded `Category` and an owned lazy many-to-many
  relationship with `Promotion` through `products_promotions`.
- `Order` belongs to one required, lazy-loaded `Customer` and owns a lazy
  `OrderItem` collection with cascade and orphan removal.
- `OrderItem` stores the purchase-time price, quantity, and required `Product`
  reference. An order cannot be persisted without at least one item.
- `OrderStatus` is stored with `EnumType.STRING` so database values remain readable.
- `CatalogDataSeeder` creates three categories and three products once at startup.

## Structure

```text
src/main/java/se/lexicon/ecommerce/
├── EcommerceApplication.java     Spring Boot entry point
├── domain/                        Part 1 and Part 2 entities
├── repository/                    Derived and JPQL repository queries
└── seed/                          Idempotent catalog startup seeder
src/main/resources/application-*.yml  H2 and MySQL application profiles
src/test/...                         Mapping, query, and seeding coverage
src/test/resources/application-test.yml Explicit H2 test configuration
```

## Part 2 submission status

- [x] **Git Branch**: Part 2 work is on `feature/jpa-part2`.
- [x] **Entities & Enums**: `Category`, `Product`, `Promotion`, `Order`, `OrderItem`, and `OrderStatus` are implemented.
- [x] **Relationships**: The required Product/Category, Order/OrderItem, Order/Customer, and Product/Promotion mappings are implemented with the specified ownership, fetch, cascade, and orphan-removal rules.
- [x] **Repositories**: The required and optional repository queries are implemented, including `@EntityGraph` order loading, category counts, and active-today promotion lookup. The latest optional-query changes are currently local edits.
- [x] **Extra Task**: `CatalogDataSeeder.java` implements idempotent category and product seeding, with categories created before products.
- [x] **Verification**: The application started with H2 and the Maven suite previously passed with 11 tests, covering schema generation, mappings, queries, N+1-safe loading, and seeding.
- [ ] **Commits**: Descriptive Part 2 commits exist, but the latest optional-query additions are still uncommitted local edits.
- [ ] **Push**: `feature/jpa-part2` is pushed through `97e6018`; the latest optional-query additions still need to be committed and pushed. Nothing from Part 2 has been merged into `main`.

At the time of this update, `feature/jpa-part2` and its remote-tracking branch pointed to `97e6018`. The current local edits are in `CategoryRepository.java`, `PromotionRepository.java`, and `Part2RepositoryQueryTest.java`; they are intentionally not committed so they can be reviewed and committed manually. `main` still points to the Part 1 line of history.

## Run and verify

Use JDK 26 (the current project JDK) and Maven 3.6.3 or later.

```powershell
mvn clean test
```

Expected result: Maven reports `BUILD SUCCESS` with eleven passing tests. The
suite confirms schema generation, Part 1 persistence and queries, Part 2
relationships, readable order status storage, N+1-safe order loading, and
idempotent catalog seeding.

```powershell
mvn spring-boot:run
```

The application uses the H2 profile by default, starts with an in-memory H2
database, and seeds the catalog during startup. The H2 console is enabled at
`http://localhost:8080/h2-console` while the application is running. To use
MySQL instead, start with the `mysql` profile and provide `DB_URL`,
`DB_USERNAME`, and `DB_PASSWORD` as needed.

On the current Windows/JDK 26 verification environment, the equivalent
project-owned cache command avoids a Java zipfs cache-access issue:

```powershell
mvn "-Dmaven.repo.local=target\maven-repository" "-Dmaven.compiler.fork=true" test
mvn "-Dmaven.repo.local=target\maven-repository" "-Dmaven.compiler.fork=true" "-Dmaven.test.skip=true" spring-boot:run "-Dspring-boot.run.jvmArguments=-Djava.io.tmpdir=target/app-tmp"
```
