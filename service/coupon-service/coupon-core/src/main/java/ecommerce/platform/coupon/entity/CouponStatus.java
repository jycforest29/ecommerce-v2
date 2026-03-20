package ecommerce.platform.coupon.entity;

import java.util.Map;
import java.util.function.Function;

public enum CouponStatus {
    ISSUED(coupon -> Map.of("userId", coupon.getUserId())),
    APPLIED(coupon -> Map.of("userId", coupon.getUserId())),
    EXPIRED(coupon -> Map.of()),
    DEACTIVATED(coupon -> Map.of("userId", coupon.getUserId())),
    APPLY_CANCELLED(coupon -> Map.of("userId", coupon.getUserId()));

    private Function<Coupon, Map<String, Object>> metadataGenerator;

    CouponStatus(Function<Coupon, Map<String, Object>> metadataGenerator) {
        this.metadataGenerator = metadataGenerator;
    }

    public Map<String, Object> generateMetaData(Coupon coupon) {
        return metadataGenerator.apply(coupon);
    }
}
