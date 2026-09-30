package se.lexicon.ecommerce.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PromotionRequest(
        @NotBlank
        @Size(max = 50)
        String code,

        @NotNull
        LocalDate startDate,

        LocalDate endDate,

        @NotNull
        @DecimalMin("0.01")
        @DecimalMax("100.00")
        BigDecimal discountPercentage,

        @NotEmpty
        List<@NotNull @Positive Long> productIds
) {

    public PromotionRequest {
        productIds = productIds == null ? null : List.copyOf(productIds);
    }
}
