package ecommerce.platform.common.event.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RefundCompletedNotificationEvent extends NotificationEvent {
    public static final String TOPIC = "notification.events.refund_completed";

    @Override
    public String getTopic() {
        return TOPIC;
    }

    protected RefundCompletedNotificationEvent() {}

    @Builder
    public RefundCompletedNotificationEvent(Long userId, String title, String body) {
        super(userId, title, body);
    }
}