package se.lexicon.ecommerce.domain;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class Part2DomainMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsCatalogRelationshipsAndStoresOrderStatusAsReadableText() {
        Category category = new Category("Electronics");
        Product product = new Product("Headphones", new BigDecimal("1299.00"), category);
        Promotion promotion = new Promotion(
                "SPRING10",
                java.time.LocalDate.of(2026, 3, 1),
                java.time.LocalDate.of(2026, 4, 30)
        );
        promotion.addProduct(product);

        Customer customer = new Customer(
                "Ada",
                "Lovelace",
                "ada.part2@example.com",
                new Address("Sveavägen 10", "Stockholm", "111 57")
        );
        Order order = new Order(customer);
        order.addItem(product, 2, new BigDecimal("1199.00"));
        order.changeStatus(OrderStatus.PAID);

        entityManager.persist(category);
        entityManager.persist(promotion);
        entityManager.persist(product);
        entityManager.persist(customer);
        entityManager.persist(order);
        entityManager.flush();
        entityManager.clear();

        Order storedOrder = entityManager.find(Order.class, order.getId());
        Product storedProduct = entityManager.find(Product.class, product.getId());

        assertThat(storedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(entityManager.createNativeQuery("select status from orders where id = :id")
                .setParameter("id", order.getId())
                .getSingleResult())
                .isEqualTo("PAID");
        assertThat(storedOrder.getItems()).hasSize(1);
        assertThat(storedOrder.getItems().getFirst().getProduct().getName()).isEqualTo("Headphones");
        assertThat(storedProduct.getCategory().getName()).isEqualTo("Electronics");
        assertThat(storedProduct.getPromotions()).extracting(Promotion::getCode).containsExactly("SPRING10");
    }

    @Test
    void rejectsOrdersWithoutItemsBeforePersisting() {
        Customer customer = new Customer(
                "Grace",
                "Hopper",
                "grace.part2@example.com",
                new Address("Kungsgatan 20", "Uppsala", "753 21")
        );
        entityManager.persist(customer);

        Order emptyOrder = new Order(customer);

        assertThatThrownBy(() -> {
            entityManager.persist(emptyOrder);
            entityManager.flush();
        })
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("an order must contain at least one item");
    }

    @Test
    void rejectsRemovingTheLastOrderItem() {
        Category category = new Category("Books");
        Product product = new Product("JPA Guide", new BigDecimal("299.00"), category);
        Customer customer = new Customer(
                "Linus",
                "Torvalds",
                "linus.part2@example.com",
                new Address("Storgatan 1", "Lund", "222 22")
        );
        Order order = new Order(customer);
        OrderItem item = order.addItem(product, 1, new BigDecimal("299.00"));

        assertThatThrownBy(() -> order.removeItem(item))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("an order must contain at least one item");

        assertThat(order.getItems()).containsExactly(item);
    }

    @Test
    void exposesOrderItemsAsReadOnlyList() {
        Category category = new Category("Games");
        Product product = new Product("Strategy Game", new BigDecimal("499.00"), category);
        Customer customer = new Customer(
                "Margaret",
                "Hamilton",
                "margaret.part2@example.com",
                new Address("Drottninggatan 5", "Stockholm", "111 51")
        );
        Order order = new Order(customer);
        order.addItem(product, 1, new BigDecimal("499.00"));

        assertThatThrownBy(() -> order.getItems().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
