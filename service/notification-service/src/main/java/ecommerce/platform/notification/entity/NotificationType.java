package ecommerce.platform.notification.entity;

import ecommerce.platform.common.event.notification.*;

public enum NotificationType {
    DELIVERY_STARTED(DeliveryStartedNotificationEvent.class),
    DELIVERY_COMPLETED(DeliveryCompletedNotificationEvent.class),
    REFUND_COMPLETED(RefundCompletedNotificationEvent.class),
    CART_ITEM_OUT_OF_STOCK(ProductOutOfStockNotificationEvent.class),
    WISHITEM_RESTOCKED(ProductRestockedNotificationEvent.class),
    COUPON_EXPIRED_SOON(CouponExpiredSoonNotificationEvent.class);

    private final Class<? extends NotificationEvent> eventClass;

    NotificationType(Class<? extends NotificationEvent> eventClass) {
        this.eventClass = eventClass;
    }

    public static NotificationType fromEvent(NotificationEvent event) {
        for (NotificationType type : values()) {
            if (type.eventClass.isInstance(event)) {
                return type;
            }
        }
        throw new IllegalArgumentException("알 수 없는 알림 이벤트: " + event.getClass().getSimpleName());
    }
}
