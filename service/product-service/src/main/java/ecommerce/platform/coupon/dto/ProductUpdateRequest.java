package ecommerce.platform.coupon.dto;

import jakarta.validation.constraints.Positive;

public record ProductUpdateRequest(
        Long imageId,
        @Positive int price
) {
}