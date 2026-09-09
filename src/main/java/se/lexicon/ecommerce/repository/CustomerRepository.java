package se.lexicon.ecommerce.repository;

import se.lexicon.ecommerce.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    List<Customer> findByLastNameIgnoreCase(String lastName);

    List<Customer> findByAddress_City(String city);

    List<Customer> findByEmailContainingIgnoreCase(String keyword);

    List<Customer> findByCreatedAtAfter(Instant date);

    List<Customer> findByCreatedAtBetween(Instant from, Instant to);

    long countByAddress_City(String city);

    boolean existsByEmail(String email);
}
