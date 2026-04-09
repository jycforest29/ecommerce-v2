package ecommerce.platform.notification.service;

import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.notification.entity.Notification;
import ecommerce.platform.notification.entity.NotificationType;
import ecommerce.platform.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NotificationManageServiceTest {

    @InjectMocks
    private NotificationManageService notificationManageService;

    @Mock
    private NotificationRepository notificationRepository;

    private Notification createNotification(Long id, Long userId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .notificationType(NotificationType.DELIVERY_STARTED)
                .dedupeKey("event-" + id)
                .title("배송 시작")
                .body("배송이 시작되었습니다.")
                .build();
        ReflectionTestUtils.setField(notification, "id", id);
        return notification;
    }

    @Test
    @DisplayName("본인 알림을 읽음 처리한다")
    void markAsReadSuccess() {
        Notification notification = createNotification(1L, 1L);
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

        notificationManageService.markAsRead(1L, 1L);

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("다른 유저의 알림 읽음 처리 시 UnauthorizedAccessException 발생")
    void markAsReadFail_differentUser() {
        Notification notification = createNotification(1L, 2L);
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationManageService.markAsRead(1L, 1L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("존재하지 않는 알림 읽음 처리 시 EntityNotFoundException 발생")
    void markAsReadFail_notFound() {
        given(notificationRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationManageService.markAsRead(1L, 999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
