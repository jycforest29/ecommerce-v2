package ecommerce.platform.coupon.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.product.StockDeductRequestEvent;
import ecommerce.platform.common.event.review.ReviewCreatedEvent;
import ecommerce.platform.common.event.review.ReviewDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final ProductEventHandler productEventHandler;

    @KafkaListener(topics = {StockDeductRequestEvent.TOPIC, ReviewCreatedEvent.TOPIC, ReviewDeletedEvent.TOPIC})
    public void handleEvent(Event event, Acknowledgment ack) {
        productEventHandler.handle(event);
        ack.acknowledge();
    }
}