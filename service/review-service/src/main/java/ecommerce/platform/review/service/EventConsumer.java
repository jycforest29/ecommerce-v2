package ecommerce.platform.review.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.order.OrderCancelledEvent;
import ecommerce.platform.common.event.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final ReviewEventHandler reviewEventHandler;

    @KafkaListener(topics = {OrderCreatedEvent.TOPIC, OrderCancelledEvent.TOPIC})
    public void handleEvent(Event event, Acknowledgment ack) {
        reviewEventHandler.handle(event);
        ack.acknowledge();
    }
}