package ecommerce.platform.common.event.notification;

import ecommerce.platform.common.event.Event;
import lombok.Getter;

@Getter
public abstract class NotificationEvent extends Event {
    private Long userId;
    private String title;
    private String body;

    protected NotificationEvent(Long userId, String title, String body) {
        super();
        this.userId = userId;
        this.title = title;
        this.body = body;
    }

    protected NotificationEvent() {
        super();
    }
}
