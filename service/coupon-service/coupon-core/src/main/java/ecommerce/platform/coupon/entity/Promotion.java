package ecommerce.platform.coupon.entity;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.coupon.util.RandomUtil;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Promotion {

    public static final String WELCOME_COUPON = "ALL::ALL::WELCOME";

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long promotionId;

    @Column(nullable = false)
    private String promotionName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int expireDays;

    @Column(nullable = false)
    private int discountRate;

    @Column(nullable = false)
    private boolean randomDiscount;

    @Column(nullable = false)
    private int minDiscountRate;

    @Column(nullable = false)
    private int maxDiscountRate;

    @Column(nullable = false)
    private int minPurchaseAmount;

    @Column(nullable = false)
    private int maxDiscountAmount;

    @Column(nullable = false)
    private Instant startedAt;

    @Column(nullable = false)
    private Instant endedAt;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Brand brand;

    @Builder
    public Promotion(String promotionName, int quantity, int expireDays,
                     int discountRate, boolean randomDiscount, int minDiscountRate, int maxDiscountRate,
                     int minPurchaseAmount, int maxDiscountAmount,
                     Instant startedAt, Instant endedAt,
                     Category category, Brand brand) {
        this.promotionName = promotionName;
        this.quantity = quantity;
        this.expireDays = expireDays;
        this.discountRate = discountRate;
        this.randomDiscount = randomDiscount;
        this.minDiscountRate = minDiscountRate;
        this.maxDiscountRate = maxDiscountRate;
        this.minPurchaseAmount = minPurchaseAmount;
        this.maxDiscountAmount = maxDiscountAmount;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.category = category;
        this.brand = brand;
    }

    public int resolveDiscountRate() {
        return randomDiscount
                ? RandomUtil.random(minDiscountRate, maxDiscountRate + 1)
                : discountRate;
    }
}
