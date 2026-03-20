package ecommerce.platform.coupon.dto;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.entity.CouponStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record CouponQueryResponse(
        Long couponId,
        String promotionName,
        int discountRate,
        int minPurchaseAmount,
        int maxDiscountAmount,
        Category category,
        Brand brand,
        CouponStatus couponStatus,
        Instant createdAt,
        Instant expiredAt
) {
    public static CouponQueryResponse from(Coupon coupon) {
        return CouponQueryResponse.builder()
                .couponId(coupon.getCouponId())
                .promotionName(coupon.getPromotion().getPromotionName())
                .discountRate(coupon.getDiscountRate())
                .minPurchaseAmount(coupon.getPromotion().getMinPurchaseAmount())
                .maxDiscountAmount(coupon.getPromotion().getMaxDiscountAmount())
                .category(coupon.getPromotion().getCategory())
                .brand(coupon.getPromotion().getBrand())
                .couponStatus(coupon.getCouponStatus())
                .createdAt(coupon.getCreatedAt())
                .expiredAt(coupon.getExpiredAt())
                .build();
    }
}
