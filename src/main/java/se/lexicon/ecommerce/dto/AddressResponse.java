package se.lexicon.ecommerce.dto;

public record AddressResponse(
        String street,
        String city,
        String zipCode
) {
}
