package se.lexicon.ecommerce.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.ecommerce.domain.Product;
import se.lexicon.ecommerce.domain.Promotion;
import se.lexicon.ecommerce.dto.ProductDiscountResponse;
import se.lexicon.ecommerce.dto.PromotionRequest;
import se.lexicon.ecommerce.dto.PromotionResponse;
import se.lexicon.ecommerce.exception.DuplicateResourceException;
import se.lexicon.ecommerce.exception.InvalidRequestException;
import se.lexicon.ecommerce.exception.ResourceNotFoundException;
import se.lexicon.ecommerce.repository.ProductRepository;
import se.lexicon.ecommerce.repository.PromotionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class PromotionServiceImpl implements PromotionService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");
    private static final BigDecimal ZERO_MONEY = new BigDecimal("0.00");

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;

    public PromotionServiceImpl(PromotionRepository promotionRepository, ProductRepository productRepository) {
        this.promotionRepository = Objects.requireNonNull(promotionRepository, "promotionRepository must not be null");
        this.productRepository = Objects.requireNonNull(productRepository, "productRepository must not be null");
    }

    @Override
    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        Objects.requireNonNull(request, "promotion request must not be null");
        String code = normalizeCode(request.code());
        if (promotionRepository.findByCodeIgnoreCase(code).isPresent()) {
            throw new DuplicateResourceException("promotion code already exists: " + code);
        }
        if (request.startDate() == null) {
            throw new InvalidRequestException("promotion startDate must not be null");
        }
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new InvalidRequestException("promotion endDate must not be before startDate");
        }
        validateDiscount(request.discountPercentage());

        List<Long> productIds = Objects.requireNonNull(request.productIds(), "productIds must not be null");
        if (productIds.isEmpty()) {
            throw new InvalidRequestException("promotion must be linked to at least one product");
        }
        if (productIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new InvalidRequestException("promotion productIds must be positive product IDs");
        }
        Set<Long> distinctIds = new HashSet<>(productIds);
        if (distinctIds.size() != productIds.size()) {
            throw new InvalidRequestException("promotion productIds must not contain duplicates");
        }

        List<Product> products = new ArrayList<>(productIds.size());
        for (Long productId : productIds) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("product not found: " + productId));
            products.add(product);
        }

        Promotion promotion = new Promotion(
                code,
                request.startDate(),
                request.endDate(),
                request.discountPercentage()
        );
        products.forEach(promotion::addProduct);
        Promotion savedPromotion = promotionRepository.save(promotion);
        productRepository.saveAll(products);
        return toResponse(savedPromotion, products);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse findById(Long id) {
        Objects.requireNonNull(id, "promotion id must not be null");
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("promotion not found: " + id));
        return toResponse(promotion, orderedProducts(promotion));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> getActivePromotions() {
        return promotionRepository.findActiveToday().stream()
                .map(promotion -> toResponse(promotion, orderedProducts(promotion)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(Product product) {
        Objects.requireNonNull(product, "product must not be null");
        return bestActivePromotion(product, LocalDate.now())
                .map(promotion -> discountAmount(product, promotion))
                .orElse(ZERO_MONEY);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDiscountResponse calculateDiscountForProduct(Long productId) {
        Objects.requireNonNull(productId, "product id must not be null");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product not found: " + productId));
        Optional<Promotion> bestPromotion = bestActivePromotion(product, LocalDate.now());
        BigDecimal amount = bestPromotion.map(promotion -> discountAmount(product, promotion)).orElse(ZERO_MONEY);
        BigDecimal discountedPrice = product.getPrice().subtract(amount).setScale(2, RoundingMode.HALF_UP);

        return new ProductDiscountResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                amount,
                discountedPrice,
                bestPromotion.map(Promotion::getCode).orElse(null)
        );
    }

    private Optional<Promotion> bestActivePromotion(Product product, LocalDate date) {
        return product.getPromotions().stream()
                .filter(promotion -> promotion.isActiveOn(date))
                .max(Comparator.comparing(Promotion::getDiscountPercentage));
    }

    private BigDecimal discountAmount(Product product, Promotion promotion) {
        return product.getPrice()
                .multiply(promotion.getDiscountPercentage())
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidRequestException("promotion code must not be blank");
        }
        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
        if (normalizedCode.length() > 50) {
            throw new InvalidRequestException("promotion code must be at most 50 characters");
        }
        return normalizedCode;
    }

    private void validateDiscount(BigDecimal discountPercentage) {
        if (discountPercentage == null
                || discountPercentage.signum() <= 0
                || discountPercentage.compareTo(ONE_HUNDRED) > 0) {
            throw new InvalidRequestException("discountPercentage must be greater than 0 and at most 100");
        }
    }

    private List<Product> orderedProducts(Promotion promotion) {
        return promotion.getProducts().stream()
                .sorted(Comparator.comparing(Product::getId))
                .toList();
    }

    private PromotionResponse toResponse(Promotion promotion, List<Product> products) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getCode(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.getDiscountPercentage(),
                products.stream().map(Product::getId).toList()
        );
    }
}
