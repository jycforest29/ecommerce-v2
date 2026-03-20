package ecommerce.platform.common.event.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductRestockedNotificationEvent extends NotificationEvent {
    public static final String TOPIC = "notification.events.product_restocked";

    @Override
    public String getTopic() {
        return TOPIC;
    }

    protected ProductRestockedNotificationEvent() {}

    @Builder
    public ProductRestockedNotificationEvent(Long userId, String title, String body) {
        super(userId, title, body);
    }
}