package se.lexicon.ecommerce.domain;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
class CustomerJpaMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsTheRequiredAddressRelationshipAndCreationTime() {
        Customer customer = new Customer(
                "Ada",
                "Lovelace",
                "ada.lovelace@example.com",
                new Address("Sveavägen 10", "Stockholm", "111 57")
        );

        entityManager.persist(customer);
        entityManager.flush();
        entityManager.clear();

        Customer storedCustomer = entityManager.find(Customer.class, customer.getId());

        assertThat(storedCustomer.getCreatedAt()).isNotNull();
        assertThat(storedCustomer.getAddress().getStreet()).isEqualTo("Sveavägen 10");
        assertThat(storedCustomer.getAddress().getCity()).isEqualTo("Stockholm");
    }
}
