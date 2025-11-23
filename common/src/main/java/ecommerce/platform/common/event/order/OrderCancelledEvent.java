package ecommerce.platform.common.event.order;

import ecommerce.platform.common.event.Event;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderCancelledEvent extends Event {
    public static final String TOPIC = "order.events.cancelled";

    private Long orderId;

    @Builder
    OrderCancelledEvent(Long orderId) {
        super();
        this.orderId = orderId;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}