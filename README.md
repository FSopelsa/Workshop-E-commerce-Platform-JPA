# E-commerce Platform JPA Workshop

This branch implements the first functional half of Part 1: a runnable Spring
Boot/JPA project and the required entity mappings for `Customer`, `Address`, and
`UserProfile`.

The customer owns a mandatory, unidirectional one-to-one address relationship.
Saving or deleting a customer cascades to its address, and orphan removal is
enabled. `createdAt` is assigned automatically just before a customer is first
persisted.

The optional bidirectional `Customer`/`UserProfile` relationship and all
repository query methods are intentionally left for the second half.

## Structure

```text
src/main/java/se/lexicon/ecommerce/
├── EcommerceApplication.java     Spring Boot entry point
└── domain/
    ├── Address.java              Required address table mapping
    ├── Customer.java             Customer and address ownership mapping
    └── UserProfile.java          Required profile table mapping
src/main/resources/application.yml H2 development configuration
src/test/.../CustomerJpaMappingTest.java JPA mapping smoke test
```

## Run and verify

Use JDK 26 (the current project JDK) and Maven 3.6.3 or later.

```powershell
mvn clean test
```

Expected result: Maven reports `BUILD SUCCESS`, and the JPA smoke test confirms
that the schema can be generated, a customer persists with its address, and the
creation timestamp is set.

```powershell
mvn spring-boot:run
```

The application starts with an in-memory H2 database. The H2 console is enabled
at `http://localhost:8080/h2-console` while the application is running.
