package ecommerce.platform.common.event.user;

import lombok.Builder;

public class UserWithdrawEvent extends UserEvent {
    public static final String TOPIC = "user.events.withdrew";

    @Builder
    public UserWithdrawEvent(Long userId) {
        super(userId);
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}