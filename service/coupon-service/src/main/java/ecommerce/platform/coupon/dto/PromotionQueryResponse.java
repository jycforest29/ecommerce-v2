package ecommerce.platform.coupon.dto;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.coupon.entity.Promotion;
import lombok.Builder;

import java.time.Instant;

@Builder
public record PromotionQueryResponse(
        Long promotionId,
        String promotionName,
        int discountRate,
        boolean randomDiscount,
        int minDiscountRate,
        int maxDiscountRate,
        int minPurchaseAmount,
        int maxDiscountAmount,
        Instant startedAt,
        Instant endedAt,
        Category category,
        Brand brand
) {
    public static PromotionQueryResponse from(Promotion promotion) {
        return PromotionQueryResponse.builder()
                .promotionId(promotion.getPromotionId())
                .promotionName(promotion.getPromotionName())
                .discountRate(promotion.getDiscountRate())
                .randomDiscount(promotion.isRandomDiscount())
                .minDiscountRate(promotion.getMinDiscountRate())
                .maxDiscountRate(promotion.getMaxDiscountRate())
                .minPurchaseAmount(promotion.getMinPurchaseAmount())
                .maxDiscountAmount(promotion.getMaxDiscountAmount())
                .startedAt(promotion.getStartedAt())
                .endedAt(promotion.getEndedAt())
                .brand(promotion.getBrand())
                .category(promotion.getCategory())
                .build();
    }
}
