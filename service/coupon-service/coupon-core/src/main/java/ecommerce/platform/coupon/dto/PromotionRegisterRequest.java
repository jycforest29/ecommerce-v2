package ecommerce.platform.coupon.dto;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.coupon.entity.Promotion;
import jakarta.validation.constraints.*;

import java.time.Instant;

public record PromotionRegisterRequest(
        @NotBlank String promotionName,
        @Positive int quantity,
        @Positive int expireDays,
        @Positive int discountRate,
        boolean randomDiscount,
        @PositiveOrZero int minDiscountRate,
        @PositiveOrZero int maxDiscountRate,
        @Positive int minPurchaseAmount,
        @Positive int maxDiscountAmount,
        @NotNull Instant startedAt,
        @NotNull Instant endedAt,
        @NotNull Category category,
        @NotNull Brand brand
) {
    public Promotion toEntity(Long userId) {
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
                .createdBy(userId)
                .category(category)
                .brand(brand)
                .build();
    }
}
