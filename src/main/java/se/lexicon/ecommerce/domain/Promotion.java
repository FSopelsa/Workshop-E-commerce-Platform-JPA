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
import java.util.Set;

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

    @ManyToMany(mappedBy = "promotions", fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();

    public Promotion(String code, LocalDate startDate, LocalDate endDate) {
        this.code = code;
        this.startDate = startDate;
        this.endDate = endDate;
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
