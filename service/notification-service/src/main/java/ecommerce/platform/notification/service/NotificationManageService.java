package ecommerce.platform.notification.service;

import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.notification.entity.Notification;
import ecommerce.platform.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationManageService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = EntityFinder.findEntity(notificationRepository, notificationId);
        if (!notification.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException();
        }
        notification.markAsRead();
    }
}