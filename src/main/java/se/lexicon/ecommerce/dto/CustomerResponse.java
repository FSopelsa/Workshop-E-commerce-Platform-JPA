package se.lexicon.ecommerce.dto;

public record CustomerResponse(
        Long id,
        String fullName,
        String email,
        AddressResponse addressResponse
) {
}
