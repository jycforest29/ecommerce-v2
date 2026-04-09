package ecommerce.platform.payment.service;

import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.payment.repository.OutboxEventRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPoller {
    private static final int MAX_RETRY_COUNT = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void pollAndPublish() {
        List<OutboxEvent> events = outboxEventRepository.findByIsPublishedFalseAndRetryCountLessThan(MAX_RETRY_COUNT);

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(event.getEntityName(), event.getEntityId(), event.getPayload());
                event.markPublished();
            } catch (Exception e) {
                log.error("이벤트 발행 실패 - outboxId: {}, entityName: {}, retryCount: {}",
                        event.getId(), event.getEntityName(), event.getRetryCount(), e);
                event.incrementRetryCount();
            }
        }
    }
}