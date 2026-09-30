package se.lexicon.ecommerce.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "promotions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @ManyToMany(mappedBy = "promotions", fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();

    public Promotion(String code, LocalDate startDate, LocalDate endDate) {
        this(code, startDate, endDate, BigDecimal.ZERO);
    }

    public Promotion(String code, LocalDate startDate, LocalDate endDate, BigDecimal discountPercentage) {
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
        this.discountPercentage = Objects.requireNonNull(discountPercentage, "discountPercentage must not be null");
        if (discountPercentage.signum() < 0 || discountPercentage.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("discountPercentage must be between 0 and 100");
        }
        this.endDate = endDate;
    }

    public boolean isActiveOn(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return !startDate.isAfter(date) && (endDate == null || !endDate.isBefore(date));
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("product must not be null");
        }
        product.addPromotion(this);
    }

    void registerProduct(Product product) {
        products.add(product);
    }

    void unregisterProduct(Product product) {
        products.remove(product);
    }
}
