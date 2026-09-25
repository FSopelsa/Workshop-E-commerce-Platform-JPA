package se.lexicon.ecommerce.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.ecommerce.domain.Address;
import se.lexicon.ecommerce.domain.Category;
import se.lexicon.ecommerce.domain.Customer;
import se.lexicon.ecommerce.domain.Product;
import se.lexicon.ecommerce.dto.CustomerRequest;
import se.lexicon.ecommerce.dto.CustomerResponse;
import se.lexicon.ecommerce.dto.OrderItemRequest;
import se.lexicon.ecommerce.dto.OrderRequest;
import se.lexicon.ecommerce.dto.ProductRequest;
import se.lexicon.ecommerce.exception.DuplicateResourceException;
import se.lexicon.ecommerce.exception.ResourceNotFoundException;
import se.lexicon.ecommerce.mapper.CustomerMapper;
import se.lexicon.ecommerce.mapper.OrderMapper;
import se.lexicon.ecommerce.mapper.ProductMapper;
import se.lexicon.ecommerce.repository.CategoryRepository;
import se.lexicon.ecommerce.repository.CustomerRepository;
import se.lexicon.ecommerce.repository.OrderRepository;
import se.lexicon.ecommerce.repository.ProductRepository;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceLayerTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    private final CustomerMapper customerMapper = new CustomerMapper();
    private final ProductMapper productMapper = new ProductMapper();
    private final OrderMapper orderMapper = new OrderMapper();

    @Test
    void registersCustomerOnlyWhenEmailIsAvailable() {
        CustomerRequest request = customerRequest("ada@example.com");
        when(customerRepository.existsByEmail(request.email())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = new CustomerServiceImpl(customerRepository, customerMapper).register(request);

        assertThat(response.fullName()).isEqualTo("Ada Lovelace");
        assertThat(response.email()).isEqualTo(request.email());
        verify(customerRepository).existsByEmail(request.email());
    }

    @Test
    void rejectsDuplicateCustomerEmail() {
        CustomerRequest request = customerRequest("ada@example.com");
        when(customerRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> new CustomerServiceImpl(customerRepository, customerMapper).register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(request.email());
    }

    @Test
    void findsAndUpdatesCustomerDetails() {
        Customer customer = new Customer(
                "Ada",
                "Lovelace",
                "old@example.com",
                new Address("Old Street", "Stockholm", "111 57")
        );
        CustomerRequest request = customerRequest("new@example.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmail(request.email())).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        CustomerService service = new CustomerServiceImpl(customerRepository, customerMapper);
        var response = service.findById(1L);
        var updated = service.update(1L, request);

        assertThat(response.email()).isEqualTo("old@example.com");
        assertThat(updated.email()).isEqualTo(request.email());
        assertThat(customer.getFirstName()).isEqualTo(request.firstName());
        assertThat(customer.getAddress().getStreet()).isEqualTo(request.street());
    }

    @Test
    void throwsWhenCustomerDoesNotExist() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new CustomerServiceImpl(customerRepository, customerMapper).findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createsProductsOnlyWithAnExistingCategoryAndSupportsQueries() {
        Category category = new Category("Books");
        Product product = new Product("JPA Guide", new BigDecimal("499.00"), category);
        when(categoryRepository.findById(7L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productRepository.findByNameContainingIgnoreCase("guide")).thenReturn(List.of(product));

        ProductService service = new ProductServiceImpl(productRepository, categoryRepository, productMapper);
        var created = service.create(new ProductRequest("JPA Guide", new BigDecimal("499.00"), 7L));

        assertThat(created.name()).isEqualTo("JPA Guide");
        assertThat(service.findAll()).extracting(response -> response.name()).containsExactly("JPA Guide");
        assertThat(service.searchByName("guide")).extracting(response -> response.categoryName())
                .containsExactly("Books");
        verify(categoryRepository).findById(7L);
    }

    @Test
    void rejectsProductsWithMissingCategory() {
        when(categoryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new ProductServiceImpl(productRepository, categoryRepository, productMapper)
                .create(new ProductRequest("JPA Guide", new BigDecimal("499.00"), 404L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void placesOrderUsingEachProductCurrentPrice() {
        Customer customer = new Customer(
                "Ada",
                "Lovelace",
                "ada.order@example.com",
                new Address("Sveavägen 10", "Stockholm", "111 57")
        );
        Category category = new Category("Books");
        Product product = new Product("JPA Guide", new BigDecimal("499.00"), category);
        when(customerRepository.findById(11L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(22L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(se.lexicon.ecommerce.domain.Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderService service = new OrderServiceImpl(orderRepository, customerRepository, productRepository, orderMapper);
        var response = service.placeOrder(new OrderRequest(
                11L,
                List.of(new OrderItemRequest(22L, 2))
        ));

        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.quantity()).isEqualTo(2);
            assertThat(item.priceAtPurchase()).isEqualByComparingTo("499.00");
        });
        verify(customerRepository).findById(11L);
        verify(productRepository).findById(22L);
    }

    @Test
    void orderPlacementIsTransactional() throws NoSuchMethodException {
        Method method = OrderServiceImpl.class.getDeclaredMethod("placeOrder", OrderRequest.class);

        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void rejectsOrderForMissingCustomer() {
        when(customerRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new OrderServiceImpl(orderRepository, customerRepository, productRepository, orderMapper)
                .placeOrder(new OrderRequest(404L, List.of(new OrderItemRequest(22L, 1)))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    private CustomerRequest customerRequest(String email) {
        return new CustomerRequest(
                "Ada",
                "Lovelace",
                email,
                "correct horse battery staple",
                "Sveavägen 10",
                "Stockholm",
                "111 57"
        );
    }

}
