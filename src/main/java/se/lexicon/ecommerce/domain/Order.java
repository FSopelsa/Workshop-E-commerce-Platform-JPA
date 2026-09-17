package se.lexicon.ecommerce.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_date", nullable = false, updatable = false)
    private Instant orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.CREATED;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Size(min = 1, message = "an order must contain at least one item")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    public Order(Customer customer) {
        this.customer = Objects.requireNonNull(customer, "customer must not be null");
    }

    public OrderItem addItem(Product product, int quantity, BigDecimal priceAtPurchase) {
        OrderItem item = new OrderItem(product, quantity, priceAtPurchase);
        addItem(item);
        return item;
    }

    public void addItem(OrderItem item) {
        Objects.requireNonNull(item, "item must not be null");
        if (item.getOrder() != this) {
            if (item.getOrder() != null) {
                item.getOrder().removeItem(item);
            }
            items.add(item);
            item.attachTo(this);
        }
    }

    public void removeItem(OrderItem item) {
        if (items.remove(item)) {
            item.detachFrom(this);
        }
    }

    public void changeStatus(OrderStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    @PrePersist
    void prepareForPersist() {
        if (orderDate == null) {
            orderDate = Instant.now();
        }
        validateItems();
    }

    @PreUpdate
    void validateBeforeUpdate() {
        validateItems();
    }

    private void validateItems() {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("an order must contain at least one item");
        }
    }
}
