package ecommerce.platform.notification.service;

import ecommerce.platform.common.event.notification.*;
import ecommerce.platform.notification.entity.Notification;
import ecommerce.platform.notification.entity.NotificationType;
import ecommerce.platform.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;

    @Transactional
    @KafkaListener(topics = {
            DeliveryStartedNotificationEvent.TOPIC,
            DeliveryCompletedNotificationEvent.TOPIC,
            RefundCompletedNotificationEvent.TOPIC,
            CouponExpiredSoonNotificationEvent.TOPIC,
            ProductOutOfStockNotificationEvent.TOPIC,
            ProductRestockedNotificationEvent.TOPIC
    })
    public void handleEvent(NotificationEvent event, Acknowledgment ack) {
        NotificationType notificationType = NotificationType.fromEvent(event);

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .notificationType(notificationType)
                .dedupeKey(event.getEventId())
                .title(event.getTitle())
                .body(event.getBody())
                .build();
        notificationRepository.save(notification);

        notificationPublisher.publish(event.getUserId(), event.getBody());
        ack.acknowledge();
    }
}
