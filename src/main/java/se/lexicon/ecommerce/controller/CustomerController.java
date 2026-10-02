package se.lexicon.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import se.lexicon.ecommerce.dto.CustomerRequest;
import se.lexicon.ecommerce.dto.CustomerResponse;
import se.lexicon.ecommerce.service.CustomerService;

import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Register, retrieve, and update customers and their addresses")
@ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = Objects.requireNonNull(customerService, "customerService must not be null");
    }

    @PostMapping
    @Operation(summary = "Register a customer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer registered", useReturnTypeSchema = true,
                    headers = @Header(name = "Location", description = "URL of the registered customer",
                            schema = @Schema(type = "string", format = "uri"))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    })
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.register(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a customer by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer found", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    public ResponseEntity<CustomerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a customer and address")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer updated", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    })
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }
}
