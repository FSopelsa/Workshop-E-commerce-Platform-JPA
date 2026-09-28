package se.lexicon.ecommerce.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import se.lexicon.ecommerce.domain.Address;
import se.lexicon.ecommerce.domain.Category;
import se.lexicon.ecommerce.domain.Customer;
import se.lexicon.ecommerce.domain.Product;
import se.lexicon.ecommerce.dto.OrderItemRequest;
import se.lexicon.ecommerce.dto.OrderRequest;
import se.lexicon.ecommerce.mapper.OrderMapper;
import se.lexicon.ecommerce.repository.CategoryRepository;
import se.lexicon.ecommerce.repository.CustomerRepository;
import se.lexicon.ecommerce.repository.OrderItemRepository;
import se.lexicon.ecommerce.repository.OrderRepository;
import se.lexicon.ecommerce.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(
        showSql = false,
        properties = "spring.datasource.url=jdbc:h2:mem:order-transaction-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_LOWER=TRUE"
)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({OrderServiceImpl.class, OrderMapper.class, PromotionServiceImpl.class})
class OrderServiceTransactionTest {

    private static final String QUANTITY_CHECK_CONSTRAINT = "chk_order_item_quantity_for_rollback_test";

    @Autowired
    private OrderService orderService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rollsBackTheWholeOrderWhenAnItemViolatesAConstraint() {
        TestData data = seedCustomerAndProducts();
        jdbcTemplate.execute("alter table order_items add constraint " + QUANTITY_CHECK_CONSTRAINT
                + " check (quantity < 2)");
        OrderRequest request = new OrderRequest(
                data.customerId(),
                List.of(
                        new OrderItemRequest(data.firstProductId(), 1),
                        new OrderItemRequest(data.secondProductId(), 2)
                )
        );

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasStackTraceContaining(QUANTITY_CHECK_CONSTRAINT);

        assertThat(orderRepository.count()).isZero();
        assertThat(orderItemRepository.count()).isZero();
    }

    private TestData seedCustomerAndProducts() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return transaction.execute(status -> {
            Category category = categoryRepository.save(new Category("Rollback Test Books"));
            Customer customer = customerRepository.save(new Customer(
                    "Ada",
                    "Lovelace",
                    "ada.rollback@example.com",
                    new Address("Test Street 1", "Stockholm", "111 57")
            ));
            Product firstProduct = productRepository.save(
                    new Product("Allowed Item", new BigDecimal("10.00"), category)
            );
            Product secondProduct = productRepository.save(
                    new Product("Rejected Item", new BigDecimal("20.00"), category)
            );
            return new TestData(customer.getId(), firstProduct.getId(), secondProduct.getId());
        });
    }

    private record TestData(Long customerId, Long firstProductId, Long secondProductId) {
    }
}
