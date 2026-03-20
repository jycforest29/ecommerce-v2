package ecommerce.platform.coupon.service;

import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.coupon.repository.OutboxEventRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventPoller {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void pollAndPublish() {
        List<OutboxEvent> events = outboxEventRepository.findByIsPublishedFalse();

        for (OutboxEvent event : events) {
            kafkaTemplate.send(event.getEntityName(), event.getEntityId(), event.getPayload());
            event.markPublished();
        }
    }
}