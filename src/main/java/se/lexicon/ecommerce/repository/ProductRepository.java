package se.lexicon.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.lexicon.ecommerce.domain.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory_NameIgnoreCase(String categoryName);

    List<Product> findByPriceBetween(BigDecimal minimumPrice, BigDecimal maximumPrice);

    List<Product> findByNameContainingIgnoreCase(String keyword);

    List<Product> findByPriceLessThan(BigDecimal price);

    List<Product> findByPriceBetweenOrderByPriceAsc(BigDecimal minimumPrice, BigDecimal maximumPrice);

    List<Product> findByPriceBetweenOrderByPriceDesc(BigDecimal minimumPrice, BigDecimal maximumPrice);

    List<Product> findByCategory_Id(Long categoryId);

    Optional<Product> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    long countByCategory_Id(Long categoryId);
}
