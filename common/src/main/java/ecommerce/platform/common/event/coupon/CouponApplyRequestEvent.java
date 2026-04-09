package ecommerce.platform.common.event.coupon;

import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CouponApplyRequestEvent extends Event {
    public static final String TOPIC = "coupon.events.apply_request";

    private Long orderId;
    private Long userId;
    private List<OrderItemInfo> orderItemInfos;

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class OrderItemInfo {
        private Long productId;
        private Long optionId;
        private int price;
        private int quantity;
    }

    @Builder
    CouponApplyRequestEvent(Long orderId, Long userId, List<OrderItemInfo> orderItemInfos) {
        super();
        this.orderId = orderId;
        this.userId = userId;
        this.orderItemInfos = orderItemInfos;
    }
}
