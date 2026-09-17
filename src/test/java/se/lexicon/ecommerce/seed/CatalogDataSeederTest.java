package se.lexicon.ecommerce.seed;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.lexicon.ecommerce.repository.CategoryRepository;
import se.lexicon.ecommerce.repository.ProductRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CatalogDataSeederTest {

    @Autowired
    private CatalogDataSeeder catalogDataSeeder;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void seedsCategoriesBeforeProductsAndIsIdempotent() {
        long categoryCount = categoryRepository.count();
        long productCount = productRepository.count();

        assertThat(categoryRepository.findByNameIgnoreCase("Electronics")).isPresent();
        assertThat(productRepository.findByCategory_NameIgnoreCase("Electronics")).isNotEmpty();

        catalogDataSeeder.run();

        assertThat(categoryRepository.count()).isEqualTo(categoryCount);
        assertThat(productRepository.count()).isEqualTo(productCount);
    }
}
