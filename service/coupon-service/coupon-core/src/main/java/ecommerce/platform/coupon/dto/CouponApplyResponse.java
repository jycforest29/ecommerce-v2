package ecommerce.platform.coupon.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record CouponApplyResponse(
        Instant appliedAt,
        int originalPrice,
        int discountAmount,
        int finalPrice
) {
    public static CouponApplyResponse from(Instant appliedAt, int originalPrice, int discountAmount) {
        return CouponApplyResponse.builder()
                .appliedAt(appliedAt)
                .originalPrice(originalPrice)
                .discountAmount(discountAmount)
                .finalPrice(originalPrice - discountAmount)
                .build();
    }
}