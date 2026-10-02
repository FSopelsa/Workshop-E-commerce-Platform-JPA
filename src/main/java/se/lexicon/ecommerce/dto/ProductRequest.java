package se.lexicon.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank
        @Size(max = 150)
        @Schema(example = "Workshop Book")
        String name,

        @NotNull
        @DecimalMin(value = "0.01")
        @Schema(example = "149.00")
        BigDecimal price,

        @NotNull
        @Positive
        @Schema(example = "1", description = "ID of an existing category")
        Long categoryId
) {
}
