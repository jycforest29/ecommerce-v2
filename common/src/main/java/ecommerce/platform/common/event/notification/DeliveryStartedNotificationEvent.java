package ecommerce.platform.common.event.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DeliveryStartedNotificationEvent extends NotificationEvent {
    public static final String TOPIC = "notification.events.delivery_started";

    @Override
    public String getTopic() {
        return TOPIC;
    }

    protected DeliveryStartedNotificationEvent() {}

    @Builder
    public DeliveryStartedNotificationEvent(Long userId, String title, String body) {
        super(userId, title, body);
    }
}
