package ecommerce.platform.order.entity;

import ecommerce.platform.common.constants.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    private Order createOrder() {
        return Order.builder()
                .userId(1L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .totalQuantity(3)
                .totalPriceSnapshot(50000)
                .build();
    }

    @Nested
    @DisplayName("주문 생성")
    class Create {

        @Test
        @DisplayName("주문이 CREATED 상태로 생성된다")
        void initialStatus() {
            Order order = createOrder();

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(order.getUserId()).isEqualTo(1L);
            assertThat(order.getTotalQuantity()).isEqualTo(3);
            assertThat(order.getTotalPriceSnapshot()).isEqualTo(50000);
            assertThat(order.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("생성 직후 쿠폰/결제 정보는 null이다")
        void noCouponOrPaymentInitially() {
            Order order = createOrder();

            assertThat(order.getCouponId()).isNull();
            assertThat(order.getDiscountRate()).isNull();
            assertThat(order.getDiscountAmount()).isNull();
            assertThat(order.getPaymentId()).isNull();
            assertThat(order.getPaidAt()).isNull();
        }
    }

    @Nested
    @DisplayName("주문 상태 전이")
    class StatusTransition {

        @Test
        @DisplayName("applyCoupon → COUPON_APPLIED 상태로 전이하고 쿠폰 정보가 설정된다")
        void applyCoupon() {
            Order order = createOrder();

            order.applyCoupon(10L, 15, 7500);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COUPON_APPLIED);
            assertThat(order.getCouponId()).isEqualTo(10L);
            assertThat(order.getDiscountRate()).isEqualTo(15);
            assertThat(order.getDiscountAmount()).isEqualTo(7500);
        }

        @Test
        @DisplayName("markStockDeducted → STOCK_DEDUCTED 상태로 전이한다")
        void markStockDeducted() {
            Order order = createOrder();

            order.markStockDeducted();

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.STOCK_DEDUCTED);
        }

        @Test
        @DisplayName("markPaymentPending → PAYMENT_PENDING 상태로 전이한다")
        void markPaymentPending() {
            Order order = createOrder();

            order.markPaymentPending();

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        }

        @Test
        @DisplayName("completePayment → PAID 상태로 전이하고 결제 정보가 설정된다")
        void completePayment() {
            Order order = createOrder();

            order.completePayment(100L, PaymentMethod.BANK_ACCOUNT);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getPaymentId()).isEqualTo(100L);
            assertThat(order.getPaidAt()).isNotNull();
            assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.BANK_ACCOUNT);
        }

        @Test
        @DisplayName("전체 흐름: CREATED → COUPON_APPLIED → STOCK_DEDUCTED → PAID")
        void fullFlow() {
            Order order = createOrder();

            order.applyCoupon(10L, 10, 5000);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COUPON_APPLIED);

            order.markStockDeducted();
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.STOCK_DEDUCTED);

            order.completePayment(100L, PaymentMethod.CREDIT_CARD);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("cancel → CANCELLED 상태로 전이한다")
        void cancel() {
            Order order = createOrder();

            order.cancel();

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("취소 가능 여부 - isCancellable")
    class IsCancellable {

        @Test
        @DisplayName("CREATED 상태이면 취소 가능하다")
        void cancellableWhenCreated() {
            Order order = createOrder();

            assertThat(order.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("COUPON_APPLIED 상태이면 취소 가능하다")
        void cancellableWhenCouponApplied() {
            Order order = createOrder();
            order.applyCoupon(10L, 10, 5000);

            assertThat(order.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("STOCK_DEDUCTED 상태이면 취소 가능하다")
        void cancellableWhenStockDeducted() {
            Order order = createOrder();
            order.markStockDeducted();

            assertThat(order.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("PAID 상태이면 취소 불가능하다")
        void notCancellableWhenPaid() {
            Order order = createOrder();
            order.completePayment(100L, PaymentMethod.CREDIT_CARD);

            assertThat(order.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("CANCELLED 상태이면 취소 불가능하다")
        void notCancellableWhenAlreadyCancelled() {
            Order order = createOrder();
            order.cancel();

            assertThat(order.isCancellable()).isFalse();
        }
    }
}
