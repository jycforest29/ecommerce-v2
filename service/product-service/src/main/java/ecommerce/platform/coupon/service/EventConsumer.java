package ecommerce.platform.coupon.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.ProcessedEvent;
import ecommerce.platform.common.event.product.StockDeductRequestEvent;
import ecommerce.platform.common.event.review.ReviewCreatedEvent;
import ecommerce.platform.common.event.review.ReviewDeletedEvent;
import ecommerce.platform.coupon.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final ProductEventHandler productEventHandler;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    @KafkaListener(topics = {StockDeductRequestEvent.TOPIC, ReviewCreatedEvent.TOPIC, ReviewDeletedEvent.TOPIC})
    public void handleEvent(Event event, Acknowledgment ack) {
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.info("중복 이벤트 무시 - eventId: {}", event.getEventId());
            ack.acknowledge();
            return;
        }
        productEventHandler.handle(event);
        processedEventRepository.save(new ProcessedEvent(event.getEventId()));
        ack.acknowledge();
    }
}