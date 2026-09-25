package se.lexicon.ecommerce.mapper;

import org.junit.jupiter.api.Test;
import se.lexicon.ecommerce.domain.Address;
import se.lexicon.ecommerce.domain.Category;
import se.lexicon.ecommerce.domain.Customer;
import se.lexicon.ecommerce.domain.Order;
import se.lexicon.ecommerce.domain.OrderStatus;
import se.lexicon.ecommerce.domain.Product;
import se.lexicon.ecommerce.dto.CustomerRequest;
import se.lexicon.ecommerce.dto.OrderItemRequest;
import se.lexicon.ecommerce.dto.OrderRequest;
import se.lexicon.ecommerce.dto.ProductRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    private final CustomerMapper customerMapper = new CustomerMapper();
    private final ProductMapper productMapper = new ProductMapper();
    private final OrderMapper orderMapper = new OrderMapper();

    @Test
    void mapsCustomerRequestAndResponseWithoutExposingTheEntity() {
        CustomerRequest request = new CustomerRequest(
                "Ada",
                "Lovelace",
                "ada@example.com",
                "correct horse battery staple",
                "Sveavägen 10",
                "Stockholm",
                "111 57"
        );

        Customer customer = customerMapper.toEntity(request);
        var response = customerMapper.toResponse(customer);

        assertThat(customer.getAddress().getCity()).isEqualTo("Stockholm");
        assertThat(response.fullName()).isEqualTo("Ada Lovelace");
        assertThat(response.email()).isEqualTo("ada@example.com");
        assertThat(response.addressResponse().street()).isEqualTo("Sveavägen 10");
    }

    @Test
    void mapsProductWithResolvedCategory() {
        Category category = new Category("Books");
        Product product = productMapper.toEntity(
                new ProductRequest("JPA Guide", new BigDecimal("499.00"), 1L),
                category
        );

        var response = productMapper.toResponse(product);

        assertThat(response.name()).isEqualTo("JPA Guide");
        assertThat(response.price()).isEqualByComparingTo("499.00");
        assertThat(response.categoryName()).isEqualTo("Books");
    }

    @Test
    void mapsOrderItemsAndCapturesCurrentProductPrices() {
        Customer customer = new Customer(
                "Ada",
                "Lovelace",
                "ada.order@example.com",
                new Address("Sveavägen 10", "Stockholm", "111 57")
        );
        Category category = new Category("Books");
        Product product = new Product("JPA Guide", new BigDecimal("499.00"), category);
        OrderRequest request = new OrderRequest(
                1L,
                List.of(new OrderItemRequest(2L, 3))
        );

        Order order = orderMapper.toEntity(request, customer, Map.of(2L, product));
        var response = orderMapper.toResponse(order);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().getFirst().getPriceAtPurchase()).isEqualByComparingTo("499.00");
        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.productName()).isEqualTo("JPA Guide");
            assertThat(item.quantity()).isEqualTo(3);
            assertThat(item.priceAtPurchase()).isEqualByComparingTo("499.00");
        });
    }
}
