package ecommerce.platform.order.dto;

import ecommerce.platform.order.entity.Order;
import ecommerce.platform.order.entity.OrderItem;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record OrderCreateResponse(
        Long userId,
        List<OrderItemResponse> orderItemResponses,
        Instant createdAt,
        Integer totalPriceSnapshot,
        Integer totalQuantity
) {
    public static OrderCreateResponse from(Order order, List<OrderItem> orderItems) {
        return OrderCreateResponse.builder()
                .userId(order.getUserId())
                .orderItemResponses(orderItems.stream().map(OrderItemResponse::of).toList())
                .createdAt(order.getCreatedAt())
                .totalPriceSnapshot(order.getTotalPriceSnapshot())
                .totalQuantity(order.getTotalQuantity())
                .build();
    }
}
