package ecommerce.platform.notification.controller;

import ecommerce.platform.common.annotations.Login;
import ecommerce.platform.notification.dto.NotificationQueryResponse;
import ecommerce.platform.notification.service.NotificationFilter;
import ecommerce.platform.notification.service.NotificationManageService;
import ecommerce.platform.notification.service.NotificationQueryService;
import ecommerce.platform.notification.service.SseEmitterHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@RestController
public class NotificationController {
    private final NotificationQueryService notificationQueryService;
    private final NotificationManageService notificationManageService;
    private final SseEmitterHandler sseEmitterHandler;

    @GetMapping
    public ResponseEntity<List<NotificationQueryResponse>> getNotifications(@Login Long userId, @RequestParam(defaultValue = "ALL") NotificationFilter notificationFilter) {
        List<NotificationQueryResponse> notificationQueryResponses = notificationQueryService.getNotifications(userId, notificationFilter);
        return ResponseEntity.ok(notificationQueryResponses);
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationQueryResponse> getNotification(@Login Long userId, @PathVariable Long notificationId) {
        NotificationQueryResponse notificationQueryResponse = notificationQueryService.getNotification(userId, notificationId);
        return ResponseEntity.ok(notificationQueryResponse);
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter stream(@Login Long userId) {
        return sseEmitterHandler.addSseEmitter(userId);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@Login Long userId, @PathVariable Long notificationId) {
        notificationManageService.markAsRead(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@Login Long userId) {
        int unreadCount = notificationQueryService.getUnreadCount(userId);
        return ResponseEntity.ok(unreadCount);
    }
}
