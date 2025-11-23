package ecommerce.platform.common.event.payment;

import ecommerce.platform.common.event.Event;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PaymentCompletedFailedEvent extends Event {
    public static final String TOPIC = "payment.events.completed_failed";

    private Long orderId;
    private Long paymentId;
    private String reason;

    @Builder
    PaymentCompletedFailedEvent(Long orderId, Long paymentId, String reason) {
        super();
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.reason = reason;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
