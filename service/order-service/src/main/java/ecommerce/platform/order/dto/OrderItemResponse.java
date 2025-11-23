package ecommerce.platform.order.dto;

import ecommerce.platform.order.entity.OrderItem;

public record OrderItemResponse(
        Long productId,
        Long productOptionId,
        int quantity,
        int priceSnapshot,
        Long couponId,
        Integer discountRate,
        Integer discountAmount
) {
    public static OrderItemResponse of(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getProductId(),
                orderItem.getProductOptionId(),
                orderItem.getQuantity(),
                orderItem.getPriceSnapshot(),
                orderItem.getCouponId(),
                orderItem.getDiscountRate(),
                orderItem.getDiscountAmount()
        );
    }
}