package ecommerce.platform.notification.service;

import ecommerce.platform.notification.dto.SseEmitterEventType;
import ecommerce.platform.notification.util.ConnectionIdGenerator;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterHandler {
    private final Map<Long, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();
    private static final long SSE_TIMEOUT = Duration.ofMinutes(30).toMillis();

    public SseEmitter addSseEmitter(Long userId) {
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT);
        String connectionId = ConnectionIdGenerator.generateId();

        emitters.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(connectionId, sseEmitter);

        sseEmitter.onCompletion(() -> removeEmitter(userId, connectionId));
        sseEmitter.onError(ex -> sseEmitter.completeWithError(ex));
        sseEmitter.onTimeout(sseEmitter::complete);

        try {
            sseEmitter.send(SseEmitter.event()
                    .name(SseEmitterEventType.CONNECTED)
                    .data("ok"));
        } catch (IOException e) {
            sseEmitter.completeWithError(e);
        }

        return sseEmitter;
    }

    public void addNewNotification(Long userId, String payload) {
        Map<String, SseEmitter> userMap = emitters.get(userId);
        if (userMap == null) return;

        userMap.forEach((connectionId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(SseEmitterEventType.NEW_NOTIFICATION)
                        .data(payload));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
    }

    private void removeEmitter(Long userId, String connectionId) {
        Map<String, SseEmitter> userMap = emitters.get(userId);
        if (userMap != null) {
            userMap.remove(connectionId);
            if (userMap.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }
}