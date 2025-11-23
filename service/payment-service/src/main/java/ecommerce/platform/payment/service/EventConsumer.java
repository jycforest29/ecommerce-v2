package ecommerce.platform.payment.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.order.OrderCancelledEvent;
import ecommerce.platform.common.event.payment.PaymentRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final PaymentEventHandler paymentEventHandler;

    @KafkaListener(topics = {PaymentRequestEvent.TOPIC, OrderCancelledEvent.TOPIC})
    public void handleEvent(Event event, Acknowledgment ack) {
        paymentEventHandler.handle(event);
        ack.acknowledge();
    }
}