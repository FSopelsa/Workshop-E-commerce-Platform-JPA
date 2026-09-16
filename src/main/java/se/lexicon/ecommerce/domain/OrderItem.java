package se.lexicon.ecommerce.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_at_purchase", nullable = false, precision = 19, scale = 2)
    private BigDecimal priceAtPurchase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public OrderItem(Product product, int quantity, BigDecimal priceAtPurchase) {
        this.product = Objects.requireNonNull(product, "product must not be null");
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least one");
        }
        this.quantity = quantity;
        this.priceAtPurchase = Objects.requireNonNull(priceAtPurchase, "priceAtPurchase must not be null");
    }

    void attachTo(Order order) {
        this.order = order;
    }

    void detachFrom(Order order) {
        if (this.order == order) {
            this.order = null;
        }
    }
}
