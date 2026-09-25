package se.lexicon.ecommerce.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.ecommerce.domain.Customer;
import se.lexicon.ecommerce.domain.Order;
import se.lexicon.ecommerce.domain.Product;
import se.lexicon.ecommerce.dto.OrderItemRequest;
import se.lexicon.ecommerce.dto.OrderRequest;
import se.lexicon.ecommerce.dto.OrderResponse;
import se.lexicon.ecommerce.exception.ResourceNotFoundException;
import se.lexicon.ecommerce.mapper.OrderMapper;
import se.lexicon.ecommerce.repository.CustomerRepository;
import se.lexicon.ecommerce.repository.OrderRepository;
import se.lexicon.ecommerce.repository.ProductRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            OrderMapper orderMapper
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository must not be null");
        this.customerRepository = Objects.requireNonNull(customerRepository, "customerRepository must not be null");
        this.productRepository = Objects.requireNonNull(productRepository, "productRepository must not be null");
        this.orderMapper = Objects.requireNonNull(orderMapper, "orderMapper must not be null");
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        Objects.requireNonNull(request, "order request must not be null");
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("customer not found: " + request.customerId()));

        Map<Long, Product> productsById = new LinkedHashMap<>();
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "product not found: " + itemRequest.productId()
                    ));
            productsById.put(itemRequest.productId(), product);
        }

        Order order = orderMapper.toEntity(request, customer, productsById);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }
}
