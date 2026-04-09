package ecommerce.platform.common.event.payment;

import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PaymentCompletedEvent extends Event {
    public static final String TOPIC = "payment.events.completed";
    private Long orderId;
    private Long paymentId;
    private PaymentMethod paymentMethod;

    @Builder
    PaymentCompletedEvent(Long orderId, Long paymentId, PaymentMethod paymentMethod) {
        super();
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
