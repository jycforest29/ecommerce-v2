package ecommerce.platform.payment.service;

import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.common.event.payment.PaymentCancelledEvent;
import ecommerce.platform.common.event.payment.PaymentCompletedEvent;
import ecommerce.platform.common.event.payment.RefundCompletedEvent;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.common.util.OutboxEventGenerator;
import ecommerce.platform.payment.controller.PGMockController.PGResponse;
import ecommerce.platform.payment.dto.PaymentCreateRequest;
import ecommerce.platform.payment.dto.PaymentCreateResponse;
import ecommerce.platform.payment.dto.PaymentQueryResponse;
import ecommerce.platform.payment.entity.Payment;
import ecommerce.platform.payment.repository.OutboxEventRepository;
import ecommerce.platform.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentManageService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PGClient pgClient;

    @Transactional
    public PaymentCreateResponse createPayment(Long orderId, PaymentCreateRequest paymentRequest, Long userId) {
        PGResponse pgResponse = pgClient.requestPayment();
        if (!pgResponse.success()) {
            throw new RuntimeException(pgResponse.message());
        }

        Payment payment = Payment.builder()
                .orderId(orderId)
                .userId(userId)
                .paymentMethod(paymentRequest.paymentMethod())
                .totalPrice(paymentRequest.totalPrice())
                .discountAmount(paymentRequest.discountAmount())
                .build();
        paymentRepository.save(payment);

        publishEvent(PaymentCompletedEvent.builder()
                .orderId(payment.getOrderId())
                .paymentId(payment.getId())
                .paymentMethod(payment.getPaymentMethod())
                .build());

        return PaymentCreateResponse.from(payment);
    }

    @Transactional
    public PaymentQueryResponse cancel(Long orderId, Long userId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("payment"));
        validateOwner(payment, userId);
        payment.cancel();

        publishEvent(PaymentCancelledEvent.builder()
                .orderId(payment.getOrderId())
                .paymentId(payment.getId())
                .build());

        return PaymentQueryResponse.from(payment);
    }

    @Transactional
    public PaymentQueryResponse refund(Long paymentId, Long userId) {
        Payment payment = EntityFinder.findEntity(paymentRepository, paymentId);
        validateOwner(payment, userId);

        PGResponse pgResponse = pgClient.requestRefund();
        if (!pgResponse.success()) {
            throw new RuntimeException(pgResponse.message());
        }

        payment.refund();

        publishEvent(RefundCompletedEvent.builder()
                .orderId(payment.getOrderId())
                .paymentId(payment.getId())
                .build());

        return PaymentQueryResponse.from(payment);
    }

    private void validateOwner(Payment payment, Long userId) {
        if (!payment.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException();
        }
    }

    private void publishEvent(ecommerce.platform.common.event.Event event) {
        OutboxEvent outboxEvent = OutboxEventGenerator.publish(event);
        outboxEventRepository.save(outboxEvent);
    }
}
