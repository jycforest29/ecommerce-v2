package ecommerce.platform.common.event.order;

import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderCancelledEvent extends Event {
    public static final String TOPIC = "order.events.cancelled";

    private Long orderId;
    private List<OrderCreatedEvent.OrderItemInfo> orderItemInfos;

    @Builder
    OrderCancelledEvent(Long orderId, List<OrderCreatedEvent.OrderItemInfo> orderItemInfos) {
        super();
        this.orderId = orderId;
        this.orderItemInfos = orderItemInfos;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}