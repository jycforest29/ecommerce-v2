package ecommerce.platform.coupon.service;

import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.entity.CouponLog;
import ecommerce.platform.coupon.entity.CouponStatus;

public class CouponLogFactory {

    public static CouponLog issue(Coupon coupon) {
        return create(coupon, CouponStatus.ISSUED);
    }

    public static CouponLog apply(Coupon coupon) {
        return create(coupon, CouponStatus.APPLIED);
    }

    public static CouponLog applyCancel(Coupon coupon) {
        return create(coupon, CouponStatus.APPLY_CANCELLED);
    }

    public static CouponLog deactivate(Coupon coupon) {
        return create(coupon, CouponStatus.DEACTIVATED);
    }

    private static CouponLog create(Coupon coupon, CouponStatus status) {
        return CouponLog.builder()
                .coupon(coupon)
                .userId(coupon.getUserId())
                .couponStatus(status)
                .metadata(status.generateMetaData(coupon))
                .build();
    }
}