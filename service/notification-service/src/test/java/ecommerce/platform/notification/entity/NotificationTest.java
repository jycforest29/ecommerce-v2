package ecommerce.platform.notification.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    private Notification buildNotification() {
        return Notification.builder()
                .userId(1L)
                .notificationType(NotificationType.DELIVERY_STARTED)
                .dedupeKey("event-123")
                .title("배송 시작")
                .body("주문하신 상품이 배송 시작되었습니다.")
                .build();
    }

    @Nested
    @DisplayName("알림 생성")
    class Create {

        @Test
        @DisplayName("알림이 정상적으로 생성된다")
        void createNotification() {
            Notification notification = buildNotification();

            assertThat(notification.getUserId()).isEqualTo(1L);
            assertThat(notification.getNotificationType()).isEqualTo(NotificationType.DELIVERY_STARTED);
            assertThat(notification.getDedupeKey()).isEqualTo("event-123");
            assertThat(notification.getTitle()).isEqualTo("배송 시작");
            assertThat(notification.getBody()).isEqualTo("주문하신 상품이 배송 시작되었습니다.");
            assertThat(notification.getCreatedAt()).isNotNull();
            assertThat(notification.getReadAt()).isNull();
        }

        @Test
        @DisplayName("생성 직후 isRead()는 false이다")
        void initiallyUnread() {
            Notification notification = buildNotification();

            assertThat(notification.isRead()).isFalse();
        }
    }

    @Nested
    @DisplayName("읽음 처리 - markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("읽음 처리하면 readAt이 설정된다")
        void markAsRead() {
            Notification notification = buildNotification();

            notification.markAsRead();

            assertThat(notification.isRead()).isTrue();
            assertThat(notification.getReadAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 읽은 알림에 다시 markAsRead를 호출해도 readAt이 변경되지 않는다")
        void markAsReadIdempotent() {
            Notification notification = buildNotification();
            notification.markAsRead();
            var firstReadAt = notification.getReadAt();

            notification.markAsRead();

            assertThat(notification.getReadAt()).isEqualTo(firstReadAt);
        }
    }
}
