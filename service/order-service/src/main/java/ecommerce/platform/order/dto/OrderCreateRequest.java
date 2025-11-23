package ecommerce.platform.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateRequest(
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