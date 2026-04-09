package ecommerce.platform.notification.service;

import ecommerce.platform.common.event.notification.DeliveryCompletedNotificationEvent;
import ecommerce.platform.common.event.notification.DeliveryStartedNotificationEvent;
import ecommerce.platform.common.event.notification.RefundCompletedNotificationEvent;
import ecommerce.platform.notification.entity.Notification;
import ecommerce.platform.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventConsumerTest {

    @InjectMocks
    private EventConsumer eventConsumer;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPublisher notificationPublisher;

    @Mock
    private Acknowledgment acknowledgment;

    @Test
    @DisplayName("DeliveryStartedNotificationEvent를 수신하면 알림을 저장하고 발행한다")
    void handleDeliveryStarted() {
        var event = DeliveryStartedNotificationEvent.builder()
                .userId(1L)
                .title("배송 시작")
                .body("주문하신 상품이 배송 시작되었습니다.")
                .build();

        eventConsumer.handleEvent(event, acknowledgment);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getTitle()).isEqualTo("배송 시작");
        assertThat(saved.getDedupeKey()).isEqualTo(event.getEventId());

        verify(notificationPublisher).publish(1L, "주문하신 상품이 배송 시작되었습니다.");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("DeliveryCompletedNotificationEvent를 수신하면 알림을 저장하고 발행한다")
    void handleDeliveryCompleted() {
        var event = DeliveryCompletedNotificationEvent.builder()
                .userId(2L)
                .title("배송 완료")
                .body("배송이 완료되었습니다.")
                .build();

        eventConsumer.handleEvent(event, acknowledgment);

        verify(notificationRepository).save(any(Notification.class));
        verify(notificationPublisher).publish(2L, "배송이 완료되었습니다.");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("RefundCompletedNotificationEvent를 수신하면 알림을 저장하고 발행한다")
    void handleRefundCompleted() {
        var event = RefundCompletedNotificationEvent.builder()
                .userId(3L)
                .title("환불 완료")
                .body("환불이 완료되었습니다.")
                .build();

        eventConsumer.handleEvent(event, acknowledgment);

        verify(notificationRepository).save(any(Notification.class));
        verify(notificationPublisher).publish(3L, "환불이 완료되었습니다.");
        verify(acknowledgment).acknowledge();
    }
}
