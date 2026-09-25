package se.lexicon.ecommerce.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal price,

        @NotNull
        @Positive
        Long categoryId
) {
}
