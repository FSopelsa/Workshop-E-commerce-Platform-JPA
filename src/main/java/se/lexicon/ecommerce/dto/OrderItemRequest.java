package se.lexicon.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull
        @Positive
        @Schema(example = "1", description = "ID of an existing product")
        Long productId,

        @Min(1)
        @Schema(example = "2")
        int quantity
) {
}
