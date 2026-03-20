package ecommerce.platform.common.event.coupon;

import ecommerce.platform.common.event.Event;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CouponRollbackApplyEvent extends Event {
    public static final String TOPIC = "coupon.events.rollback_apply";

    private Long orderId;

    protected CouponRollbackApplyEvent() {}

    @Builder
    CouponRollbackApplyEvent(Long orderId) {
        super();
        this.orderId = orderId;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
