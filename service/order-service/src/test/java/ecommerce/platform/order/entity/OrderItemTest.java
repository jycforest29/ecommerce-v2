package ecommerce.platform.order.entity;

import ecommerce.platform.common.constants.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemTest {

    @Test
    @DisplayName("OrderItem이 정상적으로 생성된다")
    void create() {
        Order order = Order.builder()
                .userId(1L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .totalQuantity(1)
                .totalPriceSnapshot(10000)
                .build();

        OrderItem item = OrderItem.builder()
                .order(order)
                .productId(1L)
                .productOptionId(2L)
                .quantity(1)
                .priceSnapshot(10000)
                .build();

        assertThat(item.getOrder()).isEqualTo(order);
        assertThat(item.getProductId()).isEqualTo(1L);
        assertThat(item.getProductOptionId()).isEqualTo(2L);
        assertThat(item.getQuantity()).isEqualTo(1);
        assertThat(item.getPriceSnapshot()).isEqualTo(10000);
        assertThat(item.getCouponId()).isNull();
    }

    @Test
    @DisplayName("applyCoupon으로 쿠폰 정보가 설정된다")
    void applyCoupon() {
        Order order = Order.builder()
                .userId(1L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .totalQuantity(1)
                .totalPriceSnapshot(10000)
                .build();

        OrderItem item = OrderItem.builder()
                .order(order)
                .productId(1L)
                .productOptionId(2L)
                .quantity(1)
                .priceSnapshot(10000)
                .build();

        item.applyCoupon(5L, 10, 1000);

        assertThat(item.getCouponId()).isEqualTo(5L);
        assertThat(item.getDiscountRate()).isEqualTo(10);
        assertThat(item.getDiscountAmount()).isEqualTo(1000);
    }
}