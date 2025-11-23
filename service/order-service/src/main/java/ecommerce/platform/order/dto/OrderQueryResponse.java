package ecommerce.platform.order.dto;

import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.order.entity.Order;
import ecommerce.platform.order.entity.OrderItem;
import ecommerce.platform.order.entity.OrderStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record OrderQueryResponse(
        Long orderId,
        Long userId,
        List<OrderItemResponse> orderItemResponses,
        Instant createdAt,
        int totalPriceSnapshot,
        int totalQuantity,
        Long couponId,
        Integer discountRate,
        Integer discountAmount,
        OrderStatus orderStatus,
        PaymentMethod paymentMethod,
        Instant paidAt
) {
    public static OrderQueryResponse from(Order order, List<OrderItem> orderItems) {
        return OrderQueryResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .orderItemResponses(orderItems.stream().map(OrderItemResponse::of).toList())
                .createdAt(order.getCreatedAt())
                .totalPriceSnapshot(order.getTotalPriceSnapshot())
                .totalQuantity(order.getTotalQuantity())
                .couponId(order.getCouponId())
                .discountRate(order.getDiscountRate())
                .discountAmount(order.getDiscountAmount())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .paidAt(order.getPaidAt())
                .build();
    }
}