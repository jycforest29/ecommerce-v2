package ecommerce.platform.common.event.payment;

import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.common.event.Event;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PaymentRequestEvent extends Event {
    public static final String TOPIC = "payment.events.request";

    private Long orderId;
    private Long userId;
    private PaymentMethod paymentMethod;
    private int discountPrice;
    private int totalPrice;

    protected PaymentRequestEvent() {}

    @Builder
    PaymentRequestEvent(Long orderId, Long userId, PaymentMethod paymentMethod, int discountPrice, int totalPrice) {
        super();
        this.orderId = orderId;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.discountPrice = discountPrice;
        this.totalPrice = totalPrice;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
