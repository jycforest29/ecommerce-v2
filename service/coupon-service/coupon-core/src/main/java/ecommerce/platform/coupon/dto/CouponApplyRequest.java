package ecommerce.platform.coupon.dto;

import ecommerce.platform.coupon.entity.CouponTargetItem;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CouponApplyRequest(
        @NotEmpty List<CouponTargetItem> orderItems
) {
}
