package se.lexicon.ecommerce.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import se.lexicon.ecommerce.domain.Address;
import se.lexicon.ecommerce.domain.Customer;
import se.lexicon.ecommerce.domain.UserProfile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
class RepositoryQueryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AddressRepository addressRepository;

    private Customer stockholmCustomer;
    private Customer uppsalaCustomer;

    @BeforeEach
    void setUp() {
        stockholmCustomer = new Customer(
                "Ada",
                "Lovelace",
                "ada@example.com",
                new Address("Sveavägen 10", "Stockholm", "111 57")
        );
        uppsalaCustomer = new Customer(
                "Grace",
                "Hopper",
                "grace@example.com",
                new Address("Kungsgatan 20", "Uppsala", "753 21")
        );

        customerRepository.saveAll(List.of(stockholmCustomer, uppsalaCustomer));
        customerRepository.flush();
        userProfileRepository.saveAll(List.of(
                new UserProfile("ada-l", "+46701234567", "Analytical thinker"),
                new UserProfile("grace-h", "+46709876543", null)
        ));
        userProfileRepository.flush();
    }

    @Test
    void customerQueriesUseEmailNameAndNestedAddressProperties() {
        assertThat(customerRepository.findByEmail("ada@example.com"))
                .get()
                .extracting(Customer::getLastName)
                .isEqualTo("Lovelace");
        assertThat(customerRepository.findByLastNameIgnoreCase("HOPPER"))
                .extracting(Customer::getEmail)
                .containsExactly("grace@example.com");
        assertThat(customerRepository.findByAddress_City("Stockholm"))
                .extracting(Customer::getEmail)
                .containsExactly("ada@example.com");
        assertThat(customerRepository.findByEmailContainingIgnoreCase("EXAMPLE"))
                .hasSize(2);
        assertThat(customerRepository.countByAddress_City("Stockholm")).isEqualTo(1);
        assertThat(customerRepository.existsByEmail("grace@example.com")).isTrue();
    }

    @Test
    void profileAndAddressQueriesUseDerivedAndCustomQueries() {
        assertThat(userProfileRepository.findByNickname("ada-l")).hasSize(1);
        assertThat(userProfileRepository.findByPhoneNumberContaining("701"))
                .hasSize(1);
        assertThat(userProfileRepository.findByBioIsNotNull()).hasSize(1);
        assertThat(userProfileRepository.findByNicknameStartingWith("grace"))
                .hasSize(1);
        assertThat(userProfileRepository.countByPhoneNumberStartingWith("+4670"))
                .isEqualTo(2);

        assertThat(addressRepository.findByZipCode("111 57")).hasSize(1);
        assertThat(addressRepository.findByCity("Stockholm")).hasSize(1);
        assertThat(addressRepository.findByStreetContainingIgnoreCase("SVEA"))
                .hasSize(1);
        assertThat(addressRepository.findByZipCodeStartingWith("111"))
                .hasSize(1);
        assertThat(addressRepository.countCustomersByZipCode("111 57"))
                .isEqualTo(1);
    }

    @Test
    void customerDateQueriesUseCreatedAt() {
        Instant after = Instant.now().minus(1, ChronoUnit.MINUTES);
        Instant before = Instant.now().plus(1, ChronoUnit.MINUTES);

        assertThat(customerRepository.findByCreatedAtAfter(after)).hasSize(2);
        assertThat(customerRepository.findByCreatedAtBetween(after, before)).hasSize(2);
    }
}
