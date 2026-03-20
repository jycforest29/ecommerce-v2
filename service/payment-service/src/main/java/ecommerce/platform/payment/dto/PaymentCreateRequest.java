package ecommerce.platform.payment.dto;

import ecommerce.platform.common.constants.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentCreateRequest(
        @NotNull PaymentMethod paymentMethod,
        @Positive int totalPrice,
        @Positive int discountAmount
) {
}