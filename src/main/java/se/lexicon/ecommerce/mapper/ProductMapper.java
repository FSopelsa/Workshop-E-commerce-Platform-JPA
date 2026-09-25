package se.lexicon.ecommerce.mapper;

import org.springframework.stereotype.Component;
import se.lexicon.ecommerce.domain.Category;
import se.lexicon.ecommerce.domain.Product;
import se.lexicon.ecommerce.dto.ProductRequest;
import se.lexicon.ecommerce.dto.ProductResponse;

import java.util.Objects;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product entity) {
        Objects.requireNonNull(entity, "product must not be null");
        Category category = Objects.requireNonNull(entity.getCategory(), "product category must not be null");

        return new ProductResponse(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                category.getName()
        );
    }

    /**
     * The service resolves the category before mapping because Product requires
     * an existing Category association rather than a category ID placeholder.
     */
    public Product toEntity(ProductRequest request, Category category) {
        Objects.requireNonNull(request, "product request must not be null");
        return new Product(
                request.name(),
                request.price(),
                Objects.requireNonNull(category, "product category must not be null")
        );
    }
}
