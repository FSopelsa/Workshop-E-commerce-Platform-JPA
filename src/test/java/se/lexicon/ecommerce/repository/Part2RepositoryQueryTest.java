package se.lexicon.ecommerce.repository;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import se.lexicon.ecommerce.domain.Address;
import se.lexicon.ecommerce.domain.Category;
import se.lexicon.ecommerce.domain.Customer;
import se.lexicon.ecommerce.domain.Order;
import se.lexicon.ecommerce.domain.OrderStatus;
import se.lexicon.ecommerce.domain.Product;
import se.lexicon.ecommerce.domain.Promotion;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class Part2RepositoryQueryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        Category electronics = categoryRepository.save(new Category("Electronics"));
        Category books = categoryRepository.save(new Category("Books"));
        product = productRepository.save(new Product("Headphones", new BigDecimal("1299.00"), electronics));
        productRepository.save(new Product("JPA Guide", new BigDecimal("499.00"), books));

        Promotion promotion = promotionRepository.save(new Promotion(
                "SPRING10",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 4, 30)
        ));
        product.addPromotion(promotion);
        productRepository.saveAndFlush(product);

        customer = customerRepository.save(new Customer(
                "Ada",
                "Lovelace",
                "ada.repository.part2@example.com",
                new Address("Sveavägen 10", "Stockholm", "111 57")
        ));
        Order order = new Order(customer);
        order.addItem(product, 2, new BigDecimal("1199.00"));
        orderRepository.saveAndFlush(order);
    }

    @Test
    void findsCategoriesAndProductsThroughRequiredNestedQueries() {
        assertThat(categoryRepository.findByNameIgnoreCase("electronics")).isPresent();
        assertThat(categoryRepository.existsByNameIgnoreCase("BOOKS")).isTrue();
        assertThat(productRepository.findByCategory_NameIgnoreCase("ELECTRONICS"))
                .extracting(Product::getName)
                .containsExactly("Headphones");
        assertThat(productRepository.findByPriceBetween(new BigDecimal("1000.00"), new BigDecimal("1500.00")))
                .extracting(Product::getName)
                .containsExactly("Headphones");
    }

    @Test
    void findsActivePromotionsAndLoadsOrderItemsWithStatusQuery() {
        assertThat(promotionRepository.findActiveOn(LocalDate.of(2026, 3, 15)))
                .extracting(Promotion::getCode)
                .containsExactly("SPRING10");

        Order loadedOrder = orderRepository.findByStatus(OrderStatus.CREATED).get(0);

        assertThat(Hibernate.isInitialized(loadedOrder.getItems())).isTrue();
        assertThat(loadedOrder.getItems()).hasSize(1);
        assertThat(orderRepository.findByCustomer_Id(customer.getId())).hasSize(1);
        assertThat(orderRepository.findByItems_Product_Id(product.getId())).hasSize(1);
    }
}
