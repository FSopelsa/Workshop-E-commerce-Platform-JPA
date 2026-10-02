package se.lexicon.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OrderRequest(
        @NotNull
        @Positive
        @Schema(example = "1", description = "ID of a registered customer")
        Long customerId,

        @NotEmpty
        List<@NotNull @Valid OrderItemRequest> items
) {

    public OrderRequest {
        items = items == null ? null : List.copyOf(items);
    }
}
