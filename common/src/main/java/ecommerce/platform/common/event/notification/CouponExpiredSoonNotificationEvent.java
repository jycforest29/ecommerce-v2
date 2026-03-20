package ecommerce.platform.common.event.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CouponExpiredSoonNotificationEvent extends NotificationEvent {
    public static final String TOPIC = "notification.events.coupon_expired_soon";

    @Override
    public String getTopic() {
        return TOPIC;
    }

    protected CouponExpiredSoonNotificationEvent() {}

    @Builder
    public CouponExpiredSoonNotificationEvent(Long userId, String title, String body) {
        super(userId, title, body);
    }
}