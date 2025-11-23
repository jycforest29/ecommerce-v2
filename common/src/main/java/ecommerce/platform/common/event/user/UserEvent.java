package ecommerce.platform.common.event.user;

import ecommerce.platform.common.event.Event;
import lombok.Getter;

@Getter
public abstract class UserEvent extends Event {
    private Long userId;

    protected UserEvent(Long userId) {
        super();
        this.userId = userId;
    }

    protected UserEvent() {
        super();
    }
}
