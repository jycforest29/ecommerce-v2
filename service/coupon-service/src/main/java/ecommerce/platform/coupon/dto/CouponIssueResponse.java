package ecommerce.platform.coupon.dto;

import ecommerce.platform.coupon.entity.Coupon;
import lombok.Builder;

import java.time.Instant;

@Builder
public record CouponIssueResponse(
        Long couponId,
        String promotionName,
        int discountRate,
        Instant createdAt,
        Instant expiredAt
) {
    public static CouponIssueResponse from(Coupon coupon) {
        return CouponIssueResponse.builder()
                .couponId(coupon.getCouponId())
                .promotionName(coupon.getPromotion().getPromotionName())
                .discountRate(coupon.getDiscountRate())
                .createdAt(coupon.getCreatedAt())
                .expiredAt(coupon.getExpiredAt())
                .build();
    }
}