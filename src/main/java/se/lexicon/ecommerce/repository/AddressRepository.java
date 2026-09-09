package se.lexicon.ecommerce.repository;

import se.lexicon.ecommerce.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByZipCode(String zipCode);

    List<Address> findByCity(String city);

    List<Address> findByStreetContainingIgnoreCase(String streetName);

    @Query("select count(c) from Customer c where c.address.zipCode = :zipCode")
    long countCustomersByZipCode(@Param("zipCode") String zipCode);

    List<Address> findByZipCodeStartingWith(String prefix);
}
