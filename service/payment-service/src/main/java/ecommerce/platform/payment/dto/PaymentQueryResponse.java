package ecommerce.platform.payment.dto;

import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.payment.entity.Payment;
import ecommerce.platform.payment.entity.PaymentStatus;

import java.time.Instant;

public record PaymentQueryResponse(
        Long paymentId,
        Long orderId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        int totalPrice,
        int discountAmount,
        int finalPrice,
        Instant createdAt
) {
    public static PaymentQueryResponse from(Payment payment) {
        return new PaymentQueryResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getTotalPrice(),
                payment.getDiscountAmount(),
                payment.getFinalPrice(),
                payment.getCreatedAt()
        );
    }
}