package ecommerce.platform.common.event.coupon;

import ecommerce.platform.common.event.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CouponAppliedEvent extends Event {
    public static final String TOPIC = "coupon.events.applied";

    private Long orderId;
    private Long couponId;
    private int discountRate;
    private int discountAmount;
    private List<CouponAppliedEvent.OrderItemInfo> orderItemInfos;

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Getter
    @AllArgsConstructor
    public static class OrderItemInfo {
        private Long productId;
        private Long optionId;
        private Long couponId;
        private int priceBeforeCouponApplied;
        private int quantity;
        private int priceAfterCouponApplied;
        private int discountRate;

        public boolean matches(Long productId, Long optionId) {
            return this.productId.equals(productId) && this.optionId.equals(optionId);
        }
    }

    @Builder
    CouponAppliedEvent(Long orderId, Long couponId, int discountRate, int discountAmount, List<CouponAppliedEvent.OrderItemInfo> orderItemInfos) {
        super();
        this.orderId = orderId;
        this.couponId = couponId;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.orderItemInfos = orderItemInfos;
    }

}
