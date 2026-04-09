package ecommerce.platform.notification.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;

class SseEmitterHandlerTest {

    private final SseEmitterHandler sseEmitterHandler = new SseEmitterHandler();

    @Test
    @DisplayName("SSE Emitter를 생성하여 반환한다")
    void addSseEmitter() {
        SseEmitter emitter = sseEmitterHandler.addSseEmitter(1L);

        assertThat(emitter).isNotNull();
        assertThat(emitter.getTimeout()).isEqualTo(30 * 60 * 1000L);
    }

    @Test
    @DisplayName("같은 유저에 대해 여러 커넥션을 등록할 수 있다")
    void multipleConnections() {
        SseEmitter emitter1 = sseEmitterHandler.addSseEmitter(1L);
        SseEmitter emitter2 = sseEmitterHandler.addSseEmitter(1L);

        assertThat(emitter1).isNotSameAs(emitter2);
    }

    @Test
    @DisplayName("연결되지 않은 유저에게 알림을 보내도 예외가 발생하지 않는다")
    void notificationToDisconnectedUser() {
        sseEmitterHandler.addNewNotification(999L, "payload");
        // 예외 없이 정상 종료
    }
}
