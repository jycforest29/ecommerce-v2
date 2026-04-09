package ecommerce.platform.common.event.payment;

import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PaymentCancelledEvent extends Event {
    public static final String TOPIC = "payment.events.cancelled";

    private Long orderId;
    private Long paymentId;

    @Builder
    PaymentCancelledEvent(Long orderId, Long paymentId) {
        super();
        this.orderId = orderId;
        this.paymentId = paymentId;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}