package se.lexicon.ecommerce.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import se.lexicon.ecommerce.domain.Order;
import se.lexicon.ecommerce.domain.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomer_Id(Long customerId);

    @EntityGraph(attributePaths = "items")
    List<Order> findByStatus(OrderStatus status);

    Optional<Order> findFirstByStatus(OrderStatus status);

    List<Order> findByOrderDateAfter(Instant date);

    List<Order> findByOrderDateBetween(Instant from, Instant to);

    List<Order> findByItems_Product_Id(Long productId);

    long countByStatus(OrderStatus status);

    List<Order> findByCustomer_IdAndStatus(Long customerId, OrderStatus status);
}
