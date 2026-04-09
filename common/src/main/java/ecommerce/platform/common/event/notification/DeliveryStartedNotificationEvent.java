package ecommerce.platform.common.event.notification;

import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DeliveryStartedNotificationEvent extends Event {
    public static final String TOPIC = "notification.events.delivery_started";

    private Long userId;
    private String title;
    private String body;

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Builder
    public DeliveryStartedNotificationEvent(Long userId, String title, String body) {
        super();
        this.userId = userId;
        this.title = title;
        this.body = body;
    }
}