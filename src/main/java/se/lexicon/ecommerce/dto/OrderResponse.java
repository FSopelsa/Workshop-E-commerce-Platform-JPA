package se.lexicon.ecommerce.dto;

import se.lexicon.ecommerce.domain.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Instant orderDate,
        OrderStatus status,
        List<OrderItemResponse> items
) {

    public OrderResponse {
        items = items == null ? null : List.copyOf(items);
    }
}
