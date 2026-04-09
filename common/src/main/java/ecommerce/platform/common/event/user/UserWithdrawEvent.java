package ecommerce.platform.common.event.user;

import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserWithdrawEvent extends Event {
    public static final String TOPIC = "user.events.withdrew";

    private Long userId;

    @Builder
    public UserWithdrawEvent(Long userId) {
        super();
        this.userId = userId;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}