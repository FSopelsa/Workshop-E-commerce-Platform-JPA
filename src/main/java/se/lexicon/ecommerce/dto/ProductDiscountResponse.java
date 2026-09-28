package se.lexicon.ecommerce.dto;

import java.math.BigDecimal;

public record ProductDiscountResponse(
        Long productId,
        String productName,
        BigDecimal originalPrice,
        BigDecimal discountAmount,
        BigDecimal discountedPrice,
        String promotionCode
) {
}
