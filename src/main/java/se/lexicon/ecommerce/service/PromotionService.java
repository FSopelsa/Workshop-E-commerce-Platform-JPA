package se.lexicon.ecommerce.service;

import se.lexicon.ecommerce.domain.Product;
import se.lexicon.ecommerce.dto.ProductDiscountResponse;
import se.lexicon.ecommerce.dto.PromotionRequest;
import se.lexicon.ecommerce.dto.PromotionResponse;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionService {

    PromotionResponse create(PromotionRequest request);

    PromotionResponse findById(Long id);

    List<PromotionResponse> getActivePromotions();

    BigDecimal calculateDiscount(Product product);

    ProductDiscountResponse calculateDiscountForProduct(Long productId);
}
