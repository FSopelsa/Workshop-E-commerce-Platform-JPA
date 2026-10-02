package se.lexicon.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(example = "WORKSHOP15")
        String code,

        @NotNull
        @Schema(example = "2026-10-01", description = "Inclusive start date")
        LocalDate startDate,

        @Schema(example = "2026-12-31", description = "Inclusive end date; omit for no expiry")
        LocalDate endDate,

        @NotNull
        @DecimalMin("0.01")
        @DecimalMax("100.00")
        @Schema(example = "15")
        BigDecimal discountPercentage,

        @NotEmpty
        @Schema(example = "[1]", description = "IDs of existing products; duplicates are not allowed")
        List<@NotNull @Positive Long> productIds
) {

    public PromotionRequest {
        productIds = productIds == null ? null : List.copyOf(productIds);
    }
}
