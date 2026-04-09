package ecommerce.platform.common.event.notification;

import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductOutOfStockNotificationEvent extends Event {
    public static final String TOPIC = "notification.events.product_out_of_stock";

    private Long userId;
    private String title;
    private String body;

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Builder
    public ProductOutOfStockNotificationEvent(Long userId, String title, String body) {
        super();
        this.userId = userId;
        this.title = title;
        this.body = body;
    }
}