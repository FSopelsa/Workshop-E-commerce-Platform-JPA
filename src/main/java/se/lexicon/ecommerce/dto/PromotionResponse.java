package se.lexicon.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PromotionResponse(
        Long id,
        String code,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal discountPercentage,
        List<Long> productIds
) {

    public PromotionResponse {
        productIds = productIds == null ? null : List.copyOf(productIds);
    }
}
