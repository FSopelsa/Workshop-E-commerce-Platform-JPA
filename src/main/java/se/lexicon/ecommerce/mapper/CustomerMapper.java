package se.lexicon.ecommerce.mapper;

import org.springframework.stereotype.Component;
import se.lexicon.ecommerce.domain.Address;
import se.lexicon.ecommerce.domain.Customer;
import se.lexicon.ecommerce.dto.AddressResponse;
import se.lexicon.ecommerce.dto.CustomerRequest;
import se.lexicon.ecommerce.dto.CustomerResponse;

import java.util.Objects;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer entity) {
        Objects.requireNonNull(entity, "customer must not be null");
        Address address = Objects.requireNonNull(entity.getAddress(), "customer address must not be null");
        String fullName = entity.getFirstName() + " " + entity.getLastName();

        return new CustomerResponse(
                entity.getId(),
                fullName,
                entity.getEmail(),
                new AddressResponse(address.getStreet(), address.getCity(), address.getZipCode())
        );
    }

    public Customer toEntity(CustomerRequest request) {
        Objects.requireNonNull(request, "customer request must not be null");

        // Customer currently has no password field. The request keeps the workshop's
        // validation contract, but credentials must not be silently persisted here.
        return new Customer(
                request.firstName(),
                request.lastName(),
                request.email(),
                new Address(request.street(), request.city(), request.zipCode())
        );
    }
}
