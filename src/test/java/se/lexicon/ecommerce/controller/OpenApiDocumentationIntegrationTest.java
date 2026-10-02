package se.lexicon.ecommerce.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@ActiveProfiles("test")
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(applicationContext).build();
    }

    @Test
    void documentsEveryVersionedOperationWithCorrectSuccessCodes() throws Exception {
        ResultActions docs = apiDocs()
                .andExpect(jsonPath("$.openapi").value(startsWith("3.")))
                .andExpect(jsonPath("$.info.title").value("Workshop E-commerce API"))
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.paths.length()").value(13))
                .andExpect(jsonPath("$.tags[*].name").value(containsInAnyOrder(
                        "Customers", "Products", "Categories", "Orders", "Promotions")));

        Map<String, List<String>> endpoints = Map.ofEntries(
                Map.entry("/api/v1/customers", List.of("post")),
                Map.entry("/api/v1/customers/{id}", List.of("get", "put")),
                Map.entry("/api/v1/products", List.of("get", "post")),
                Map.entry("/api/v1/products/{id}", List.of("get")),
                Map.entry("/api/v1/products/search", List.of("get")),
                Map.entry("/api/v1/categories", List.of("get", "post")),
                Map.entry("/api/v1/categories/{id}", List.of("get")),
                Map.entry("/api/v1/orders", List.of("post")),
                Map.entry("/api/v1/orders/{id}", List.of("get")),
                Map.entry("/api/v1/promotions", List.of("post")),
                Map.entry("/api/v1/promotions/active", List.of("get")),
                Map.entry("/api/v1/promotions/{id}", List.of("get")),
                Map.entry("/api/v1/promotions/products/{productId}/discount", List.of("get"))
        );
        for (Map.Entry<String, List<String>> endpoint : endpoints.entrySet()) {
            for (String method : endpoint.getValue()) {
                String operation = "$.paths['" + endpoint.getKey() + "']." + method;
                String successCode = method.equals("post") ? "201" : "200";
                docs.andExpect(jsonPath(operation + ".summary").isNotEmpty())
                        .andExpect(jsonPath(operation + ".responses['" + successCode + "'].content").isNotEmpty())
                        .andExpect(jsonPath(operation + ".responses['500']['$ref']")
                                .value("#/components/responses/InternalServerError"));
                if (method.equals("post")) {
                    docs.andExpect(jsonPath(operation + ".responses['200']").doesNotExist())
                            .andExpect(jsonPath(operation + ".responses['201'].headers.Location").exists());
                }
            }
        }
    }

    @Test
    void exposesRequestSchemasConstraintsExamplesAndRequiredSearchParameter() throws Exception {
        apiDocs()
                .andExpect(jsonPath("$.components.schemas.CustomerRequest.required").value(hasItem("password")))
                .andExpect(jsonPath("$.components.schemas.CustomerRequest.properties.password.writeOnly").value(true))
                .andExpect(jsonPath("$.components.schemas.CustomerRequest.properties.password.format").value("password"))
                .andExpect(jsonPath("$.components.schemas.CustomerRequest.properties.password.minLength").value(8))
                .andExpect(jsonPath("$.components.schemas.CustomerRequest.properties.email.example").value("ada@example.com"))
                .andExpect(jsonPath("$.components.schemas.CustomerResponse.properties.password").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.ProductRequest.properties.categoryId.example").value(1))
                .andExpect(jsonPath("$.components.schemas.OrderRequest.properties.items.minItems").value(1))
                .andExpect(jsonPath("$.components.schemas.OrderItemRequest.properties.quantity.minimum").value(1))
                .andExpect(jsonPath("$.paths['/api/v1/products/search'].get.parameters[0].name").value("name"))
                .andExpect(jsonPath("$.paths['/api/v1/products/search'].get.parameters[0].in").value("query"))
                .andExpect(jsonPath("$.paths['/api/v1/products/search'].get.parameters[0].required").value(true));
    }

    @Test
    void documentsReusableProblemDetailsWithoutOverwritingSuccessSchemas() throws Exception {
        apiDocs()
                .andExpect(jsonPath("$.components.responses.BadRequest.content['application/problem+json'].schema['$ref']")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.components.schemas.ProblemDetail.properties.status.type").value("integer"))
                .andExpect(jsonPath("$.paths['/api/v1/customers'].post.responses['409']['$ref']")
                        .value("#/components/responses/Conflict"))
                .andExpect(jsonPath("$.paths['/api/v1/orders'].post.responses['404']['$ref']")
                        .value("#/components/responses/NotFound"))
                .andExpect(jsonPath("$.paths['/api/v1/products/search'].get.responses['400']['$ref']")
                        .value("#/components/responses/BadRequest"))
                .andExpect(jsonPath("$.paths['/api/v1/customers'].post.responses['201'].content.*.schema['$ref']")
                        .value(hasItem("#/components/schemas/CustomerResponse")))
                .andExpect(jsonPath("$.paths['/api/v1/products'].get.responses['200'].content.*.schema.items['$ref']")
                        .value(hasItem("#/components/schemas/ProductResponse")));
    }

    @Test
    void servesSwaggerUiItsAssetsAndTheApiDocsConfiguration() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/swagger-ui/index.html"));
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
        mockMvc.perform(get("/swagger-ui/swagger-ui-bundle.js"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/swagger-ui/swagger-ui.css"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/v3/api-docs"));
    }

    private ResultActions apiDocs() throws Exception {
        return mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
