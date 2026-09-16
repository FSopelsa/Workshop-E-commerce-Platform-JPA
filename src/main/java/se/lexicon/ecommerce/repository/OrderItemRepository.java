package se.lexicon.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.lexicon.ecommerce.domain.OrderItem;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_Id(Long orderId);

    List<OrderItem> findByProduct_Id(Long productId);

    List<OrderItem> findByQuantityGreaterThan(Integer quantity);
}
