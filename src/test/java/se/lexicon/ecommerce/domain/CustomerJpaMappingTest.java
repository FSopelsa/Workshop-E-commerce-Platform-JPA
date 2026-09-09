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

    @Test
    void persistsTheOptionalBidirectionalProfileRelationship() {
        Customer customer = new Customer(
                "Grace",
                "Hopper",
                "grace.hopper@example.com",
                new Address("Kungsgatan 20", "Uppsala", "753 21")
        );
        UserProfile profile = new UserProfile("grace-h", "+46709876543", "Compiler pioneer");
        customer.assignProfile(profile);

        entityManager.persist(customer);
        entityManager.flush();
        entityManager.clear();

        Customer storedCustomer = entityManager.find(Customer.class, customer.getId());

        assertThat(storedCustomer.getProfile().getNickname()).isEqualTo("grace-h");
        assertThat(storedCustomer.getProfile().getCustomer().getEmail())
                .isEqualTo("grace.hopper@example.com");
    }

    @Test
    void keepsBothSidesConsistentWhenProfileIsReplacedOrCleared() {
        Customer customer = new Customer(
                "Ada",
                "Lovelace",
                "ada.profile@example.com",
                new Address("Sveavägen 10", "Stockholm", "111 57")
        );
        UserProfile firstProfile = new UserProfile("ada-first", "+46701234567", "First profile");
        UserProfile secondProfile = new UserProfile("ada-second", "+46707654321", "Second profile");

        customer.assignProfile(firstProfile);
        customer.assignProfile(secondProfile);

        assertThat(firstProfile.getCustomer()).isNull();
        assertThat(secondProfile.getCustomer()).isSameAs(customer);

        customer.assignProfile(null);

        assertThat(customer.getProfile()).isNull();
        assertThat(secondProfile.getCustomer()).isNull();
    }
}
