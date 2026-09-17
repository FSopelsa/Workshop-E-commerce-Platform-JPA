package se.lexicon.ecommerce.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.ecommerce.domain.Category;
import se.lexicon.ecommerce.domain.Product;
import se.lexicon.ecommerce.repository.CategoryRepository;
import se.lexicon.ecommerce.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogDataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(String... args) {
        Category electronics = findOrCreateCategory("Electronics");
        Category books = findOrCreateCategory("Books");
        Category home = findOrCreateCategory("Home");

        seedProduct(
                "Noise-cancelling headphones",
                new BigDecimal("1299.00"),
                electronics,
                List.of("https://example.test/images/headphones-front.jpg")
        );
        seedProduct(
                "Practical JPA with Spring",
                new BigDecimal("499.00"),
                books,
                List.of("https://example.test/images/jpa-book.jpg")
        );
        seedProduct(
                "Bamboo desk organizer",
                new BigDecimal("249.00"),
                home,
                List.of("https://example.test/images/desk-organizer.jpg")
        );
    }

    private Category findOrCreateCategory(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(new Category(name)));
    }

    private void seedProduct(String name, BigDecimal price, Category category, List<String> imageUrls) {
        if (!productRepository.existsByNameIgnoreCase(name)) {
            productRepository.save(new Product(name, price, category, imageUrls));
        }
    }
}
