package se.lexicon.ecommerce.dto;

/** Response contract returned by the category service and REST API. */
public record CategoryResponse(
        Long id,
        String name
) {
}
