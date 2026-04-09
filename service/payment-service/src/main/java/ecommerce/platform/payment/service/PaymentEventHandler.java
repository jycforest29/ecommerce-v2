package ecommerce.platform.payment.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.common.event.order.OrderCancelledEvent;
import ecommerce.platform.common.event.payment.PaymentCancelledEvent;
import ecommerce.platform.common.event.payment.PaymentCompletedEvent;
import ecommerce.platform.common.event.payment.PaymentFailedEvent;
import ecommerce.platform.common.event.payment.PaymentRequestEvent;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.payment.controller.PGMockController;
import ecommerce.platform.common.util.OutboxEventGenerator;
import ecommerce.platform.payment.entity.Payment;
import ecommerce.platform.payment.repository.OutboxEventRepository;
import ecommerce.platform.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentEventHandler {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PGClient pgClient;

    @Transactional
    public void handle(Event event) {
        if (event instanceof PaymentRequestEvent e) {
            handlePaymentRequest(e);
        } else if (event instanceof OrderCancelledEvent e) {
            handleOrderCancelled(e);
        }
    }

    private void handlePaymentRequest(PaymentRequestEvent event) {
        PGMockController.PGResponse pgResponse = pgClient.requestPayment();
        if (!pgResponse.success()) {
            publishEvent(PaymentFailedEvent.builder()
                    .orderId(event.getOrderId())
                    .reason(pgResponse.message())
                    .build());
            return;
        }

        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .paymentMethod(event.getPaymentMethod())
                .totalPrice(event.getTotalPrice())
                .discountAmount(event.getDiscountPrice())
                .build();
        paymentRepository.save(payment);

        publishEvent(PaymentCompletedEvent.builder()
                .orderId(payment.getOrderId())
                .paymentId(payment.getId())
                .paymentMethod(payment.getPaymentMethod())
                .build());
    }

    private void handleOrderCancelled(OrderCancelledEvent event) {
        Payment payment = paymentRepository.findByOrderId(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("payment"));
        payment.cancel();

        publishEvent(PaymentCancelledEvent.builder()
                .orderId(payment.getOrderId())
                .paymentId(payment.getId())
                .build());
    }

    private void publishEvent(Event event) {
        OutboxEvent outboxEvent = OutboxEventGenerator.publish(event);
        outboxEventRepository.save(outboxEvent);
    }
}