package se.lexicon.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OrderRequest(
        @NotNull
        @Positive
        Long customerId,

        @NotEmpty
        List<@NotNull @Valid OrderItemRequest> items
) {

    public OrderRequest {
        items = items == null ? null : List.copyOf(items);
    }
}
