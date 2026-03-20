package ecommerce.platform.notification.dto;

import ecommerce.platform.notification.entity.Notification;
import ecommerce.platform.notification.entity.NotificationType;
import lombok.Builder;

import java.time.Instant;

@Builder
public record NotificationQueryResponse(
        Long notificationId,
        NotificationType notificationType,
        String title,
        String body,
        Instant createdAt,
        Instant readAt
) {
    public static NotificationQueryResponse from(Notification notification) {
        return NotificationQueryResponse.builder()
                .notificationId(notification.getId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
