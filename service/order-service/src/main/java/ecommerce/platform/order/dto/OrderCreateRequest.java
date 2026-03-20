package ecommerce.platform.order.dto;

import ecommerce.platform.common.constants.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(
        @NotNull PaymentMethod paymentMethod,
        @Valid @NotEmpty List<OrderItemRequest> orderItemRequests
) {
    public int getQuantity() {
        return orderItemRequests.stream()
                .mapToInt(OrderItemRequest::quantity)
                .sum();
    }

    public int getPriceSnapshot() {
        return orderItemRequests.stream()
                .mapToInt(OrderItemRequest::price)
                .sum();
    }
}