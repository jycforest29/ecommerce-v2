package ecommerce.platform.order.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.ProcessedEvent;
import ecommerce.platform.common.event.coupon.CouponAppliedEvent;
import ecommerce.platform.common.event.payment.PaymentCompletedEvent;
import ecommerce.platform.common.event.payment.PaymentFailedEvent;
import ecommerce.platform.common.event.product.StockDeductedEvent;
import ecommerce.platform.order.repository.ProcessedEventRepository;
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

    private final OrderSagaHandler orderSagaHandler;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    @KafkaListener(topics = {CouponAppliedEvent.TOPIC, StockDeductedEvent.TOPIC, PaymentCompletedEvent.TOPIC, PaymentFailedEvent.TOPIC})
    public void handleEvent(Event event, Acknowledgment ack) {
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.info("중복 이벤트 무시 - eventId: {}", event.getEventId());
            ack.acknowledge();
            return;
        }
        orderSagaHandler.handle(event);
        processedEventRepository.save(new ProcessedEvent(event.getEventId()));
        ack.acknowledge();
    }
}