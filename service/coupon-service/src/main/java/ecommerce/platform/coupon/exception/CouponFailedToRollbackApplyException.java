package ecommerce.platform.coupon.exception;

public class CouponFailedToRollbackApplyException extends RuntimeException {

    public CouponFailedToRollbackApplyException() {
        super("쿠폰 적용 취소에 실패했습니다.");
    }
}