package se.lexicon.ecommerce.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "image_order")
    @Column(name = "image_url", nullable = false, length = 500)
    private List<String> imageUrls = new ArrayList<>();

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "products_promotions",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "promotion_id")
    )
    private Set<Promotion> promotions = new HashSet<>();

    public Product(String name, BigDecimal price, Category category) {
        this.name = name;
        this.price = price;
        assignCategory(category);
    }

    public Product(String name, BigDecimal price, Category category, List<String> imageUrls) {
        this(name, price, category);
        if (imageUrls != null) {
            this.imageUrls.addAll(imageUrls);
        }
    }

    public void assignCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("category must not be null");
        }
        if (this.category == category) {
            category.registerProduct(this);
            return;
        }
        if (this.category != null) {
            this.category.unregisterProduct(this);
        }
        this.category = category;
        category.registerProduct(this);
    }

    public void addPromotion(Promotion promotion) {
        if (promotion == null) {
            throw new IllegalArgumentException("promotion must not be null");
        }
        if (promotions.add(promotion)) {
            promotion.registerProduct(this);
        }
    }

    public void removePromotion(Promotion promotion) {
        if (promotions.remove(promotion)) {
            promotion.unregisterProduct(this);
        }
    }
}
