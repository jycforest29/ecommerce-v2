package ecommerce.platform.common.event.payment;

import ecommerce.platform.common.event.Event;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RefundCompletedEvent extends Event {
    public static final String TOPIC = "payment.events.refund_completed";

    private Long orderId;
    private Long paymentId;

    protected RefundCompletedEvent() {}

    @Builder
    RefundCompletedEvent(Long orderId, Long paymentId) {
        super();
        this.orderId = orderId;
        this.paymentId = paymentId;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}