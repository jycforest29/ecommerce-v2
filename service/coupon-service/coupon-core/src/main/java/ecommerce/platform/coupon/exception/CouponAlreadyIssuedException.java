package ecommerce.platform.coupon.exception;

public class CouponAlreadyIssuedException extends RuntimeException {

    public CouponAlreadyIssuedException(Long promotionId, Long userId) {
        super("이미 발급된 쿠폰입니다. promotionId=" + promotionId + ", userId=" + userId);
    }
}