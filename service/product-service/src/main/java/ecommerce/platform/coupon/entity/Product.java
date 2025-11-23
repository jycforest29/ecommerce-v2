package ecommerce.platform.coupon.entity;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = true)
    private Long imageId;

    @Enumerated(EnumType.STRING)
    private Brand brand;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(nullable = false)
    private int price;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int reviewCount = 0;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = true)
    private Instant modifiedAt;

    @Column(nullable = true)
    private Instant deletedAt;

    @Column(nullable = false)
    private boolean inSale;

    @Builder
    public Product(Long sellerId, String name, Long imageId, Brand brand, Category category,
                   int price, String description) {
        this.sellerId = sellerId;
        this.name = name;
        this.imageId = imageId;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.description = description;
        this.inSale = true;
        this.createdAt = Instant.now();
    }

    public void modify(Long imageId, int price) {
        this.imageId = imageId;
        this.price = price;
        this.modifiedAt = Instant.now();
    }

    public void delete() {
        this.deletedAt = Instant.now();
        this.inSale = false;
    }
}