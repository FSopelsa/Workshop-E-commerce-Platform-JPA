package se.lexicon.ecommerce.controller;

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
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = Objects.requireNonNull(promotionService, "promotionService must not be null");
    }

    @PostMapping
    public ResponseEntity<PromotionResponse> create(@Valid @RequestBody PromotionRequest request) {
        PromotionResponse response = promotionService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/active")
    public List<PromotionResponse> getActivePromotions() {
        return promotionService.getActivePromotions();
    }

    @GetMapping("/products/{productId}/discount")
    public ProductDiscountResponse calculateDiscount(@PathVariable Long productId) {
        return promotionService.calculateDiscountForProduct(productId);
    }

    @GetMapping("/{id}")
    public PromotionResponse findById(@PathVariable Long id) {
        return promotionService.findById(id);
    }
}
