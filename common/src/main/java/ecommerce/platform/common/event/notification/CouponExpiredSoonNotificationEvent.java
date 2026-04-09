package ecommerce.platform.common.event.notification;

import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CouponExpiredSoonNotificationEvent extends Event {
    public static final String TOPIC = "notification.events.coupon_expired_soon";

    private Long userId;
    private String title;
    private String body;

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Builder
    public CouponExpiredSoonNotificationEvent(Long userId, String title, String body) {
        super();
        this.userId = userId;
        this.title = title;
        this.body = body;
    }
}
