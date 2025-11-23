package ecommerce.platform.coupon.entity;

import ecommerce.platform.common.constants.ClothingSize;
import ecommerce.platform.common.constants.Color;
import ecommerce.platform.common.constants.ShoeSize;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ClothingSize clothingSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ShoeSize shoeSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Color color;

    @Column(nullable = false)
    private int additionalPrice;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private boolean available;

    @Builder
    public ProductOption(Product product, ClothingSize clothingSize, ShoeSize shoeSize,
                         Color color, int additionalPrice, int stock) {
        this.product = product;
        this.clothingSize = clothingSize;
        this.shoeSize = shoeSize;
        this.color = color;
        this.additionalPrice = additionalPrice;
        this.stock = stock;
        this.available = true;
    }

    public void deductStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.stock -= quantity;
        if (this.stock == 0) {
            this.available = false;
        }
    }

    public void restoreStock(int quantity) {
        this.stock += quantity;
        this.available = true;
    }
}