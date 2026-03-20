package ecommerce.platform.coupon.dto;

import ecommerce.platform.coupon.entity.Promotion;
import lombok.Builder;

@Builder
public record PromotionRegisterResponse(
        Long promotionId,
        String promotionName,
        int quantity
) {
    public static PromotionRegisterResponse from(Promotion promotion) {
        return PromotionRegisterResponse.builder()
                .promotionId(promotion.getPromotionId())
                .promotionName(promotion.getPromotionName())
                .quantity(promotion.getQuantity())
                .build();
    }
}
