package ecommerce.platform.order.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.coupon.CouponAppliedEvent;
import ecommerce.platform.common.event.payment.PaymentCompletedEvent;
import ecommerce.platform.common.event.payment.PaymentFailedEvent;
import ecommerce.platform.common.event.product.StockDeductedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final OrderSagaHandler orderSagaHandler;

    @KafkaListener(topics = {CouponAppliedEvent.TOPIC, StockDeductedEvent.TOPIC, PaymentCompletedEvent.TOPIC, PaymentFailedEvent.TOPIC})
    public void handleEvent(Event event, Acknowledgment ack) {
        orderSagaHandler.handle(event);
        ack.acknowledge();
    }
}