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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import se.lexicon.ecommerce.dto.ProductDiscountResponse;
import se.lexicon.ecommerce.dto.PromotionRequest;
import se.lexicon.ecommerce.dto.PromotionResponse;
import se.lexicon.ecommerce.service.PromotionService;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/promotions")
@Tag(name = "Promotions", description = "Create promotions and preview the highest active discount")
@ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = Objects.requireNonNull(promotionService, "promotionService must not be null");
    }

    @PostMapping
    @Operation(summary = "Create a promotion", description = "Dates are inclusive; omit endDate for no expiry")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Promotion created", useReturnTypeSchema = true,
                    headers = @Header(name = "Location", description = "URL of the created promotion",
                            schema = @Schema(type = "string", format = "uri"))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    })
    public ResponseEntity<PromotionResponse> create(@Valid @RequestBody PromotionRequest request) {
        PromotionResponse response = promotionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/active")
    @Operation(summary = "List promotions active today")
    @ApiResponse(responseCode = "200", description = "Active promotions", useReturnTypeSchema = true)
    public List<PromotionResponse> getActivePromotions() {
        return promotionService.getActivePromotions();
    }

    @GetMapping("/products/{productId}/discount")
    @Operation(summary = "Preview a product's best active discount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Best available discount", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    public ProductDiscountResponse calculateDiscount(@PathVariable Long productId) {
        return promotionService.calculateDiscountForProduct(productId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a promotion by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion found", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    public PromotionResponse findById(@PathVariable Long id) {
        return promotionService.findById(id);
    }
}
