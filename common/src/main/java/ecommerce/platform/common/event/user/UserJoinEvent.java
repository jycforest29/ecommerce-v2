package ecommerce.platform.common.event.user;

import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserJoinEvent extends Event {
    public static final String TOPIC = "user.events.joined";

    private Long userId;

    @Builder
    public UserJoinEvent(Long userId) {
        super();
        this.userId = userId;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
