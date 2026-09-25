package se.lexicon.ecommerce.mapper;

import org.springframework.stereotype.Component;
import se.lexicon.ecommerce.domain.Customer;
import se.lexicon.ecommerce.domain.Order;
import se.lexicon.ecommerce.domain.OrderItem;
import se.lexicon.ecommerce.domain.Product;
import se.lexicon.ecommerce.dto.OrderItemRequest;
import se.lexicon.ecommerce.dto.OrderItemResponse;
import se.lexicon.ecommerce.dto.OrderRequest;
import se.lexicon.ecommerce.dto.OrderResponse;

import java.util.Map;
import java.util.Objects;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order entity) {
        Objects.requireNonNull(entity, "order must not be null");

        return new OrderResponse(
                entity.getId(),
                entity.getOrderDate(),
                entity.getStatus(),
                entity.getItems().stream()
                        .map(this::toItemResponse)
                        .toList()
        );
    }

    /**
     * Products are resolved by the service so this mapper can build the order
     * without creating transient or detached category/product references.
     */
    public Order toEntity(OrderRequest request, Customer customer, Map<Long, Product> productsById) {
        Objects.requireNonNull(request, "order request must not be null");
        Order order = new Order(Objects.requireNonNull(customer, "order customer must not be null"));
        Map<Long, Product> resolvedProducts = Objects.requireNonNull(productsById, "products must not be null");

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = resolvedProducts.get(itemRequest.productId());
            if (product == null) {
                throw new IllegalArgumentException("product must be resolved before mapping: " + itemRequest.productId());
            }
            order.addItem(product, itemRequest.quantity(), product.getPrice());
        }

        return order;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        Product product = Objects.requireNonNull(item.getProduct(), "order item product must not be null");
        return new OrderItemResponse(
                product.getId(),
                product.getName(),
                item.getQuantity(),
                item.getPriceAtPurchase()
        );
    }
}
