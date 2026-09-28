package se.lexicon.ecommerce.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import se.lexicon.ecommerce.domain.OrderStatus;
import se.lexicon.ecommerce.dto.CategoryRequest;
import se.lexicon.ecommerce.dto.CustomerRequest;
import se.lexicon.ecommerce.dto.OrderItemRequest;
import se.lexicon.ecommerce.dto.OrderRequest;
import se.lexicon.ecommerce.dto.ProductRequest;
import se.lexicon.ecommerce.dto.PromotionRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommerceApiIntegrationTest {

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(applicationContext).build();
    }

    @Test
    void supportsCategoryCustomerProductPromotionAndOrderApiWorkflows() throws Exception {
        long categoryId = createCategory("Workshop Gear");
        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Workshop Gear"));
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", org.hamcrest.Matchers.hasItem("Workshop Gear")));

        CustomerRequest customerRequest = customerRequest("api.customer@example.com", "Ada");
        String customerJson = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(customerRequest.email()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long customerId = objectMapper.readTree(customerJson).get("id").asLong();

        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest("api.customer@example.com", "Augusta"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Augusta Lovelace"));

        ProductRequest productRequest = new ProductRequest("Workshop JPA Book", new BigDecimal("500.00"), categoryId);
        String productJson = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName").value("Workshop Gear"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long productId = objectMapper.readTree(productJson).get("id").asLong();

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId));
        mockMvc.perform(get("/api/products").queryParam("name", "JPA Book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(productId));

        createPromotion("workshop15", new BigDecimal("15.00"),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(7),
                productId);
        long bestPromotionId = createPromotion("workshop25", new BigDecimal("25.00"),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(7), productId);
        createPromotion("expired90", new BigDecimal("90.00"),
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(1), productId);

        mockMvc.perform(get("/api/promotions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].code", org.hamcrest.Matchers.containsInAnyOrder(
                        "WORKSHOP15",
                        "WORKSHOP25"
                )));
        mockMvc.perform(get("/api/promotions/{id}", bestPromotionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("WORKSHOP25"));
        mockMvc.perform(get("/api/promotions/products/{id}/discount", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountAmount").value(125.0))
                .andExpect(jsonPath("$.discountedPrice").value(375.0))
                .andExpect(jsonPath("$.promotionCode").value("WORKSHOP25"));

        OrderRequest orderRequest = new OrderRequest(
                customerId,
                List.of(new OrderItemRequest(productId, 2))
        );
        String orderJson = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.name()))
                .andExpect(jsonPath("$.items[0].priceAtPurchase").value(375.0))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long orderId = objectMapper.readTree(orderJson).get("id").asLong();

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value(productId));
    }

    @Test
    void returnsProblemDetailsForValidationDuplicatesAndMissingResources() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest(" "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("name")));

        createCategory("Unique API Category");
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("unique api category"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        mockMvc.perform(get("/api/customers/{id}", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Invalid\",\"price\":0,\"categoryId\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        String validCustomer = objectMapper.writeValueAsString(customerRequest("duplicate.api@example.com", "Ada"));
        mockMvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(validCustomer))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(validCustomer))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Resource conflict"));
    }

    private long createCategory(String name) throws Exception {
        String response = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest(name))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createPromotion(
            String code,
            BigDecimal discountPercentage,
            LocalDate startDate,
            LocalDate endDate,
            long productId
    ) throws Exception {
        PromotionRequest request = new PromotionRequest(
                code,
                startDate,
                endDate,
                discountPercentage,
                List.of(productId)
        );
        String response = mockMvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(code.toUpperCase()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private CustomerRequest customerRequest(String email, String firstName) {
        return new CustomerRequest(
                firstName,
                "Lovelace",
                email,
                "example-only-password",
                "Test Street 1",
                "Stockholm",
                "111 57"
        );
    }
}
