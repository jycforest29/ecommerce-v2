package ecommerce.platform.coupon.dto;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.coupon.entity.Promotion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record PromotionRegisterRequest(
        @NotBlank String promotionName,
        @Positive int quantity,
        @Positive int expireDays,
        int discountRate,
        boolean randomDiscount,
        int minDiscountRate,
        int maxDiscountRate,
        @Positive int minPurchaseAmount,
        @Positive int maxDiscountAmount,
        @NotNull Instant startedAt,
        @NotNull Instant endedAt,
        @NotNull Category category,
        @NotNull Brand brand
) {
    public Promotion toEntity() {
        return Promotion.builder()
                .promotionName(promotionName)
                .quantity(quantity)
                .expireDays(expireDays)
                .discountRate(discountRate)
                .randomDiscount(randomDiscount)
                .minDiscountRate(minDiscountRate)
                .maxDiscountRate(maxDiscountRate)
                .minPurchaseAmount(minPurchaseAmount)
                .maxDiscountAmount(maxDiscountAmount)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .category(category)
                .brand(brand)
                .build();
    }
}
