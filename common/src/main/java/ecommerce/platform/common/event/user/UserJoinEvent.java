package ecommerce.platform.common.event.user;

import lombok.Builder;

public class UserJoinEvent extends UserEvent {
    public static final String TOPIC = "user.events.joined";

    @Builder
    public UserJoinEvent(Long userId) {
        super(userId);
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}