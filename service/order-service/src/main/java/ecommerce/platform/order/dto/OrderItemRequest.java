package ecommerce.platform.order.dto;

import ecommerce.platform.order.entity.Order;
import ecommerce.platform.order.entity.OrderItem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull Long productId,
        @NotNull Long productOptionId,
        @Positive int quantity,
        @Positive int price
) {
    public OrderItem toEntity(Order order) {
        return OrderItem.builder()
                .order(order)
                .productId(productId)
                .productOptionId(productOptionId)
                .quantity(quantity)
                .priceSnapshot(price)
                .build();
    }
}