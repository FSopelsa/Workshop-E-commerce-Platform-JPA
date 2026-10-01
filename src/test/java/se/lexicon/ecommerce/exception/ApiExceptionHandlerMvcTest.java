package se.lexicon.ecommerce.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import se.lexicon.ecommerce.controller.CategoryController;
import se.lexicon.ecommerce.controller.CustomerController;
import se.lexicon.ecommerce.controller.OrderController;
import se.lexicon.ecommerce.controller.ProductController;
import se.lexicon.ecommerce.controller.PromotionController;
import se.lexicon.ecommerce.dto.CustomerRequest;
import se.lexicon.ecommerce.dto.PromotionRequest;
import se.lexicon.ecommerce.service.CategoryService;
import se.lexicon.ecommerce.service.CustomerService;
import se.lexicon.ecommerce.service.OrderService;
import se.lexicon.ecommerce.service.ProductService;
import se.lexicon.ecommerce.service.PromotionService;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ApiExceptionHandlerMvcTest {

    private static final String CUSTOMER_JSON = """
            {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com",
             "password":"private-test-password","street":"Test Street 1",
             "city":"Stockholm","zipCode":"11122"}
            """;

    private final CustomerService customerService = mock(CustomerService.class);
    private final ProductService productService = mock(ProductService.class);
    private final CategoryService categoryService = mock(CategoryService.class);
    private final OrderService orderService = mock(OrderService.class);
    private final PromotionService promotionService = mock(PromotionService.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(
                new CustomerController(customerService),
                new ProductController(productService),
                new CategoryController(categoryService),
                new OrderController(orderService),
                new PromotionController(promotionService)
        ).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"customers", "products", "categories", "orders", "promotions"})
    void returnsConsistentNotFoundProblemsAcrossControllers(String resource) throws Exception {
        String detail = "resource not found: 42";
        when(customerService.findById(42L)).thenThrow(new ResourceNotFoundException(detail));
        when(productService.findById(42L)).thenThrow(new ResourceNotFoundException(detail));
        when(categoryService.findById(42L)).thenThrow(new ResourceNotFoundException(detail));
        when(orderService.findById(42L)).thenThrow(new ResourceNotFoundException(detail));
        when(promotionService.findById(42L)).thenThrow(new ResourceNotFoundException(detail));

        String path = "/api/v1/" + resource + "/42";
        assertProblem(get(path), 404, "Resource not found", path)
                .andExpect(jsonPath("$.detail").value(detail));
    }

    @Test
    void returnsConflictForDuplicateCustomerEmail() throws Exception {
        when(customerService.register(any(CustomerRequest.class)))
                .thenThrow(new DuplicateResourceException("customer email is already registered"));

        assertProblem(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON).content(CUSTOMER_JSON),
                409, "Resource conflict", "/api/v1/customers")
                .andExpect(jsonPath("$.detail").value("customer email is already registered"));
    }

    @Test
    void returnsBadRequestForInvalidServiceRules() throws Exception {
        when(promotionService.create(any(PromotionRequest.class)))
                .thenThrow(new InvalidRequestException("promotion endDate must not be before startDate"));

        assertProblem(post("/api/v1/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"INVALID-DATES","startDate":"2026-10-02","endDate":"2026-10-01",
                                 "discountPercentage":10,"productIds":[1]}
                                """),
                400, "Invalid request", "/api/v1/promotions")
                .andExpect(jsonPath("$.detail").value("promotion endDate must not be before startDate"));
    }

    @ParameterizedTest
    @MethodSource("invalidRequestBodies")
    void returnsFieldValidationDetailsWithoutRejectedValues(String path, String body, String field) throws Exception {
        String response = assertProblem(post(path).contentType(MediaType.APPLICATION_JSON).content(body),
                400, "Validation failed", path)
                .andExpect(jsonPath("$.detail").value(containsString(field)))
                .andReturn().getResponse().getContentAsString();

        assertThat(response).doesNotContain("private-test-password");
    }

    static Stream<Arguments> invalidRequestBodies() {
        return Stream.of(
                Arguments.of("/api/v1/customers", CUSTOMER_JSON.replace("ada@example.com", "not-an-email"), "email"),
                Arguments.of("/api/v1/products", "{\"name\":\"Book\",\"price\":0,\"categoryId\":1}", "price"),
                Arguments.of("/api/v1/categories", "{\"name\":\" \"}", "name"),
                Arguments.of("/api/v1/orders", "{\"customerId\":1,\"items\":[{\"productId\":1,\"quantity\":0}]}", "items[0].quantity")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"customers", "products", "categories", "orders", "promotions"})
    void returnsBadRequestForInvalidIdentifiersAcrossControllers(String resource) throws Exception {
        String path = "/api/v1/" + resource + "/not-a-number";
        assertProblem(get(path), 400, "Malformed request", path)
                .andExpect(jsonPath("$.detail").value("Request values could not be read"));
    }

    @Test
    void returnsBadRequestForMalformedJson() throws Exception {
        assertProblem(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"private-test-password\","),
                400, "Malformed request", "/api/v1/customers")
                .andExpect(jsonPath("$.detail").value("Request values could not be read"));
    }

    @Test
    void returnsBadRequestForMissingRequestBody() throws Exception {
        assertProblem(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON),
                400, "Malformed request", "/api/v1/orders");
    }

    @Test
    void returnsProblemDetailsForMissingSearchParameter() throws Exception {
        assertProblem(get("/api/v1/products/search"), 400, "Bad Request", "/api/v1/products/search")
                .andExpect(jsonPath("$.detail").value(containsString("name")));
    }

    @Test
    void returnsProblemDetailsForUnsupportedMethods() throws Exception {
        assertProblem(delete("/api/v1/products"), 405, "Method Not Allowed", "/api/v1/products")
                .andExpect(header().string("Allow", containsString("GET")))
                .andExpect(header().string("Allow", containsString("POST")));
    }

    @Test
    void returnsProblemDetailsForUnsupportedContentTypes() throws Exception {
        assertProblem(post("/api/v1/customers").contentType(MediaType.TEXT_PLAIN).content(CUSTOMER_JSON),
                415, "Unsupported Media Type", "/api/v1/customers");
    }

    @Test
    void returnsProblemDetailsForUnknownRoutes() throws Exception {
        assertProblem(get("/api/v1/unknown-resource"), 404, "Not Found", "/api/v1/unknown-resource");
    }

    @Test
    void hidesUnexpectedFailureDetails() throws Exception {
        when(productService.findAll()).thenThrow(new IllegalStateException("test-internal-database-secret"));

        String response = assertProblem(get("/api/v1/products"), 500, "Internal server error", "/api/v1/products")
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred. Please try again later."))
                .andReturn().getResponse().getContentAsString();

        assertThat(response).doesNotContain("test-internal-database-secret", "IllegalStateException", "stackTrace");
    }

    private ResultActions assertProblem(MockHttpServletRequestBuilder request, int statusCode,
                                        String title, String instance) throws Exception {
        return mockMvc.perform(request)
                .andExpect(status().is(statusCode))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(statusCode))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.instance").value(instance));
    }
}
