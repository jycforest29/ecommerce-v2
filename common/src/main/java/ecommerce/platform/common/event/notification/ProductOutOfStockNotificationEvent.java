package ecommerce.platform.common.event.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductOutOfStockNotificationEvent extends NotificationEvent {
    public static final String TOPIC = "notification.events.product_out_of_stock";

    @Override
    public String getTopic() {
        return TOPIC;
    }

    protected ProductOutOfStockNotificationEvent() {}

    @Builder
    public ProductOutOfStockNotificationEvent(Long userId, String title, String body) {
        super(userId, title, body);
    }
}