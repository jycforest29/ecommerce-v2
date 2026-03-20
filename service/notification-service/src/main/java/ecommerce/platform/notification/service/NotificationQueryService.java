package ecommerce.platform.notification.service;

import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.notification.dto.NotificationQueryResponse;
import ecommerce.platform.notification.entity.Notification;
import ecommerce.platform.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {
    private final NotificationRepository notificationRepository;

    public List<NotificationQueryResponse> getNotifications(Long userId, NotificationFilter notificationFilter) {
        List<Notification> notifications = switch (notificationFilter) {
            case ALL -> notificationRepository.findByUserId(userId);
            case READ -> notificationRepository.findByUserIdRead(userId);
            case UNREAD -> notificationRepository.findByUserIdUnread(userId);
        };

        return notifications.stream()
                .map(NotificationQueryResponse::from)
                .toList();
    }

    public NotificationQueryResponse getNotification(Long userId, Long notificationId) {
        Notification notification = EntityFinder.findEntity(notificationRepository, notificationId);
        if (!notification.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException();
        }
        return NotificationQueryResponse.from(notification);
    }

    public int getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdUnread(userId);
    }
}