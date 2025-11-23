package ecommerce.platform.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSubscriber {
    private final SseEmitterHandler sseEmitterHandler;

    public void onMessage(String message) {
        String[] parsedMessage = message.split(":", 2);
        try {
            Long userId = Long.parseLong(parsedMessage[0]);
            String payload = parsedMessage[1];
            sseEmitterHandler.addNewNotification(userId, payload);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.warn("알림 메시지 파싱 실패: {}", message, e);
        }
    }
}