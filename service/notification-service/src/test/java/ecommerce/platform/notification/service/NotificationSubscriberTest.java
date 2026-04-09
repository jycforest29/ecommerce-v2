package ecommerce.platform.notification.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationSubscriberTest {

    @InjectMocks
    private NotificationSubscriber notificationSubscriber;

    @Mock
    private SseEmitterHandler sseEmitterHandler;

    @Test
    @DisplayName("유효한 메시지를 파싱하여 SseEmitterHandler에 전달한다")
    void validMessage() {
        notificationSubscriber.onMessage("42:배송이 시작되었습니다.");

        verify(sseEmitterHandler).addNewNotification(42L, "배송이 시작되었습니다.");
    }

    @Test
    @DisplayName("payload에 콜론이 포함된 경우에도 첫 번째 콜론만 기준으로 분리한다")
    void messageWithColonInPayload() {
        notificationSubscriber.onMessage("1:시간: 14:30에 도착 예정");

        verify(sseEmitterHandler).addNewNotification(1L, "시간: 14:30에 도착 예정");
    }

    @Test
    @DisplayName("userId가 숫자가 아닌 메시지는 무시한다")
    void invalidUserId() {
        notificationSubscriber.onMessage("abc:payload");

        verify(sseEmitterHandler, never()).addNewNotification(anyLong(), anyString());
    }

    @Test
    @DisplayName("콜론이 없는 메시지는 무시한다")
    void noColonMessage() {
        notificationSubscriber.onMessage("invalid-message");

        verify(sseEmitterHandler, never()).addNewNotification(anyLong(), anyString());
    }
}
