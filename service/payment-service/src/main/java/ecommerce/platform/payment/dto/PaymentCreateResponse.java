package ecommerce.platform.payment.dto;

import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.payment.entity.Payment;

import java.time.Instant;

public record PaymentCreateResponse(
        Long paymentId,
        Long orderId,
        PaymentMethod paymentMethod,
        int finalPrice,
        Instant createdAt
) {
    public static PaymentCreateResponse from(Payment payment) {
        return new PaymentCreateResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentMethod(),
                payment.getFinalPrice(),
                payment.getCreatedAt()
        );
    }
}
