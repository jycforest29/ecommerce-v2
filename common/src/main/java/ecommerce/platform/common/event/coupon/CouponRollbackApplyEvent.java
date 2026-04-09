package ecommerce.platform.common.event.coupon;

import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CouponRollbackApplyEvent extends Event {
    public static final String TOPIC = "coupon.events.rollback_apply";

    private Long orderId;
    private Long couponId;

    @Builder
    CouponRollbackApplyEvent(Long orderId, Long couponId) {
        super();
        this.orderId = orderId;
        this.couponId = couponId;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
