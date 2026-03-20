package ecommerce.platform.coupon.service;

import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.entity.CouponLog;
import ecommerce.platform.coupon.entity.CouponStatus;

import java.util.Map;

public class CouponLogFactory {

    public static CouponLog apply(Coupon coupon) {
        return generate(coupon, CouponStatus.APPLIED);
    }

    public static CouponLog applyCancel(Coupon coupon) {
        return generate(coupon, CouponStatus.APPLY_CANCELLED);
    }

    public static CouponLog issue(Coupon coupon) {
        return generate(coupon, CouponStatus.ISSUED);
    }

    public static CouponLog expire(Coupon coupon) {
        return generate(coupon, CouponStatus.EXPIRED);
    }

    public static CouponLog deactivate(Coupon coupon) { return generate(coupon, CouponStatus.DEACTIVATED);}

    private static CouponLog generate(Coupon coupon, CouponStatus couponStatus) {
        final Map<String, Object> metadata = couponStatus.generateMetaData(coupon);

        return CouponLog.builder()
                .coupon(coupon)
                .userId(coupon.getUserId())
                .couponStatus(couponStatus)
                .metadata(metadata)
                .build();
    }
}
