package se.lexicon.ecommerce.dto;

/**
 * Response contract for the category service introduced in the optional
 * advanced-services part of the workshop.
 */
@SuppressWarnings("unused")
public record CategoryResponse(
        Long id,
        String name
) {
}
