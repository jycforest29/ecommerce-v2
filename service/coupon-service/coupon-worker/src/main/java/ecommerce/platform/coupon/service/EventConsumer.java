package ecommerce.platform.coupon.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.ProcessedEvent;
import ecommerce.platform.common.event.coupon.CouponApplyRequestEvent;
import ecommerce.platform.common.event.coupon.CouponRollbackApplyEvent;
import ecommerce.platform.common.event.user.UserJoinEvent;
import ecommerce.platform.common.event.user.UserWithdrawEvent;
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

    private final CouponEventHandler couponEventHandler;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    @KafkaListener(topics = {
            UserJoinEvent.TOPIC, UserWithdrawEvent.TOPIC,
            CouponApplyRequestEvent.TOPIC, CouponRollbackApplyEvent.TOPIC
    })
    public void handleEvent(Event event, Acknowledgment ack) {
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.info("중복 이벤트 무시 - eventId: {}", event.getEventId());
            ack.acknowledge();
            return;
        }
        couponEventHandler.handle(event);
        processedEventRepository.save(new ProcessedEvent(event.getEventId()));
        ack.acknowledge();
    }
}