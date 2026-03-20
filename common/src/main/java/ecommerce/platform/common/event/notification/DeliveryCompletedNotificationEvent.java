package ecommerce.platform.common.event.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DeliveryCompletedNotificationEvent extends NotificationEvent {
    public static final String TOPIC = "notification.events.delivery_completed";

    @Override
    public String getTopic() {
        return TOPIC;
    }

    protected DeliveryCompletedNotificationEvent() {}

    @Builder
    public DeliveryCompletedNotificationEvent(Long userId, String title, String body) {
        super(userId, title, body);
    }
}
