package ecommerce.platform.common.event.payment;

import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PaymentFailedEvent extends Event {
    public static final String TOPIC = "payment.events.failed";

    private Long orderId;
    private String reason;

    @Builder
    PaymentFailedEvent(Long orderId, String reason) {
        super();
        this.orderId = orderId;
        this.reason = reason;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
