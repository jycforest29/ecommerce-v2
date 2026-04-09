package ecommerce.platform.notification.entity;

import ecommerce.platform.common.event.notification.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationTypeTest {

    @Test
    @DisplayName("DeliveryStartedNotificationEvent → DELIVERY_STARTED")
    void deliveryStarted() {
        var event = DeliveryStartedNotificationEvent.builder()
                .userId(1L).title("t").body("b").build();

        assertThat(NotificationType.fromEvent(event)).isEqualTo(NotificationType.DELIVERY_STARTED);
    }

    @Test
    @DisplayName("DeliveryCompletedNotificationEvent → DELIVERY_COMPLETED")
    void deliveryCompleted() {
        var event = DeliveryCompletedNotificationEvent.builder()
                .userId(1L).title("t").body("b").build();

        assertThat(NotificationType.fromEvent(event)).isEqualTo(NotificationType.DELIVERY_COMPLETED);
    }

    @Test
    @DisplayName("RefundCompletedNotificationEvent → REFUND_COMPLETED")
    void refundCompleted() {
        var event = RefundCompletedNotificationEvent.builder()
                .userId(1L).title("t").body("b").build();

        assertThat(NotificationType.fromEvent(event)).isEqualTo(NotificationType.REFUND_COMPLETED);
    }

    @Test
    @DisplayName("CouponExpiredSoonNotificationEvent → COUPON_EXPIRED_SOON")
    void couponExpiredSoon() {
        var event = CouponExpiredSoonNotificationEvent.builder()
                .userId(1L).title("t").body("b").build();

        assertThat(NotificationType.fromEvent(event)).isEqualTo(NotificationType.COUPON_EXPIRED_SOON);
    }

    @Test
    @DisplayName("ProductOutOfStockNotificationEvent → CART_ITEM_OUT_OF_STOCK")
    void productOutOfStock() {
        var event = ProductOutOfStockNotificationEvent.builder()
                .userId(1L).title("t").body("b").build();

        assertThat(NotificationType.fromEvent(event)).isEqualTo(NotificationType.CART_ITEM_OUT_OF_STOCK);
    }

    @Test
    @DisplayName("ProductRestockedNotificationEvent → WISHITEM_RESTOCKED")
    void productRestocked() {
        var event = ProductRestockedNotificationEvent.builder()
                .userId(1L).title("t").body("b").build();

        assertThat(NotificationType.fromEvent(event)).isEqualTo(NotificationType.WISHITEM_RESTOCKED);
    }

    @Test
    @DisplayName("매핑되지 않는 이벤트는 IllegalArgumentException 발생")
    void unknownEvent() {
        NotificationEvent unknownEvent = new NotificationEvent(1L, "t", "b") {
            @Override
            public String getTopic() {
                return "unknown";
            }
        };

        assertThatThrownBy(() -> NotificationType.fromEvent(unknownEvent))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
