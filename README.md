# E-commerce Platform JPA Workshop

This branch implements the runnable Spring Boot/JPA foundation for Part 1,
including the required entity mappings and repository query layer for
`Customer`, `Address`, and `UserProfile`.

The customer owns a mandatory, unidirectional one-to-one address relationship.
Saving or deleting a customer cascades to its address, and orphan removal is
enabled. `createdAt` is assigned automatically just before a customer is first
persisted.

The customer also owns an optional bidirectional one-to-one profile relationship;
the profile exposes the inverse side with `mappedBy`.

## Structure

```text
src/main/java/se/lexicon/ecommerce/
├── EcommerceApplication.java     Spring Boot entry point
└── domain/
    ├── Address.java              Required address table mapping
    ├── Customer.java             Customer and address ownership mapping
    └── UserProfile.java          Required profile table mapping
src/main/java/.../repository/     Derived and JPQL repository queries
src/main/resources/application.yml H2 development configuration
src/test/.../CustomerJpaMappingTest.java Focused mapping smoke test
src/test/.../RepositoryQueryTest.java Repository query coverage
```

## Run and verify

Use JDK 26 (the current project JDK) and Maven 3.6.3 or later.

```powershell
mvn clean test
```

Expected result: Maven reports `BUILD SUCCESS` with four passing tests. The suite
confirms schema generation, customer/address persistence, automatic creation
timestamps, and the required repository queries.

```powershell
mvn spring-boot:run
```

The application starts with an in-memory H2 database. The H2 console is enabled
at `http://localhost:8080/h2-console` while the application is running.
