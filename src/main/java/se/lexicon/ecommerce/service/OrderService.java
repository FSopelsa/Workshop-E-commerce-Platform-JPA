package se.lexicon.ecommerce.service;

import se.lexicon.ecommerce.dto.OrderRequest;
import se.lexicon.ecommerce.dto.OrderResponse;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request);

    OrderResponse findById(Long id);
}
