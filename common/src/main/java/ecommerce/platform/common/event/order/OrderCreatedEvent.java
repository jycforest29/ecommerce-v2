package ecommerce.platform.common.event.order;

import ecommerce.platform.common.event.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import ecommerce.platform.common.constants.Category;

import java.util.List;

@Getter
public class OrderCreatedEvent extends Event {
    public static final String TOPIC = "order.events.created";

    private Long orderId;
    private Long userId;
    private List<OrderItemInfo> orderItemInfos;

    protected OrderCreatedEvent() {}

    @Builder
    OrderCreatedEvent(Long orderId, Long userId, List<OrderItemInfo> orderItemInfos) {
        super();
        this.orderId = orderId;
        this.userId = userId;
        this.orderItemInfos = orderItemInfos;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Getter
    @AllArgsConstructor
    public static class OrderItemInfo {
        private Long productId;
        private Long productOptionId;
        private String productName;
        private Long imageId;
        private Category category;

        protected OrderItemInfo() {}
    }
}