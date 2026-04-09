package ecommerce.platform.notification.service;

import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.notification.dto.NotificationQueryResponse;
import ecommerce.platform.notification.entity.Notification;
import ecommerce.platform.notification.entity.NotificationType;
import ecommerce.platform.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {

    @InjectMocks
    private NotificationQueryService notificationQueryService;

    @Mock
    private NotificationRepository notificationRepository;

    private Notification createNotification(Long id, Long userId, boolean read) {
        Notification notification = Notification.builder()
                .userId(userId)
                .notificationType(NotificationType.DELIVERY_STARTED)
                .dedupeKey("event-" + id)
                .title("알림 " + id)
                .body("알림 내용 " + id)
                .build();
        ReflectionTestUtils.setField(notification, "id", id);
        if (read) notification.markAsRead();
        return notification;
    }

    @Nested
    @DisplayName("알림 목록 조회 - getNotifications")
    class GetNotifications {

        @Test
        @DisplayName("ALL 필터 - 전체 알림을 반환한다")
        void getAllNotifications() {
            var n1 = createNotification(1L, 1L, false);
            var n2 = createNotification(2L, 1L, true);
            given(notificationRepository.findByUserId(1L)).willReturn(List.of(n1, n2));

            List<NotificationQueryResponse> result = notificationQueryService.getNotifications(1L, NotificationFilter.ALL);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("READ 필터 - 읽은 알림만 반환한다")
        void getReadNotifications() {
            var n1 = createNotification(1L, 1L, true);
            given(notificationRepository.findByUserIdRead(1L)).willReturn(List.of(n1));

            List<NotificationQueryResponse> result = notificationQueryService.getNotifications(1L, NotificationFilter.READ);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).readAt()).isNotNull();
        }

        @Test
        @DisplayName("UNREAD 필터 - 읽지 않은 알림만 반환한다")
        void getUnreadNotifications() {
            var n1 = createNotification(1L, 1L, false);
            given(notificationRepository.findByUserIdUnread(1L)).willReturn(List.of(n1));

            List<NotificationQueryResponse> result = notificationQueryService.getNotifications(1L, NotificationFilter.UNREAD);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).readAt()).isNull();
        }

        @Test
        @DisplayName("알림이 없으면 빈 리스트를 반환한다")
        void noNotifications() {
            given(notificationRepository.findByUserId(1L)).willReturn(List.of());

            assertThat(notificationQueryService.getNotifications(1L, NotificationFilter.ALL)).isEmpty();
        }
    }

    @Nested
    @DisplayName("알림 단건 조회 - getNotification")
    class GetNotification {

        @Test
        @DisplayName("본인 알림을 조회한다")
        void getSuccess() {
            var notification = createNotification(1L, 1L, false);
            given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

            NotificationQueryResponse result = notificationQueryService.getNotification(1L, 1L);

            assertThat(result.notificationId()).isEqualTo(1L);
            assertThat(result.title()).isEqualTo("알림 1");
        }

        @Test
        @DisplayName("다른 유저의 알림 조회 시 UnauthorizedAccessException 발생")
        void getFail_differentUser() {
            var notification = createNotification(1L, 2L, false);
            given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

            assertThatThrownBy(() -> notificationQueryService.getNotification(1L, 1L))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }

        @Test
        @DisplayName("존재하지 않는 알림 조회 시 EntityNotFoundException 발생")
        void getFail_notFound() {
            given(notificationRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> notificationQueryService.getNotification(1L, 999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("안읽은 알림 수 조회 - getUnreadCount")
    class GetUnreadCount {

        @Test
        @DisplayName("안읽은 알림 수를 반환한다")
        void unreadCount() {
            given(notificationRepository.countByUserIdUnread(1L)).willReturn(5);

            assertThat(notificationQueryService.getUnreadCount(1L)).isEqualTo(5);
        }

        @Test
        @DisplayName("안읽은 알림이 없으면 0을 반환한다")
        void noUnread() {
            given(notificationRepository.countByUserIdUnread(1L)).willReturn(0);

            assertThat(notificationQueryService.getUnreadCount(1L)).isZero();
        }
    }
}
