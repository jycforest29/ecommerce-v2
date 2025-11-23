package ecommerce.platform.coupon.exception;

public class CouponFailedToApplyException extends RuntimeException {

    public CouponFailedToApplyException() {
        super("쿠폰 적용에 실패했습니다.");
    }
}