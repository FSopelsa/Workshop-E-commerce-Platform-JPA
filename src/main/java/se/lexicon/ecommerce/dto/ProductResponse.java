package se.lexicon.ecommerce.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        String categoryName
) {
}
