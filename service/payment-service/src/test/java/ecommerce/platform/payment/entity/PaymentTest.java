package ecommerce.platform.payment.entity;

import ecommerce.platform.common.constants.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    private Payment createPayment() {
        return Payment.builder()
                .orderId(1L)
                .userId(1L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .totalPrice(50000)
                .discountAmount(5000)
                .build();
    }

    @Nested
    @DisplayName("결제 생성")
    class Create {

        @Test
        @DisplayName("결제가 COMPLETED 상태로 생성된다")
        void initialStatus() {
            Payment payment = createPayment();

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(payment.getOrderId()).isEqualTo(1L);
            assertThat(payment.getUserId()).isEqualTo(1L);
            assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
            assertThat(payment.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("finalPrice는 totalPrice - discountAmount로 계산된다")
        void finalPriceCalculation() {
            Payment payment = createPayment();

            assertThat(payment.getTotalPrice()).isEqualTo(50000);
            assertThat(payment.getDiscountAmount()).isEqualTo(5000);
            assertThat(payment.getFinalPrice()).isEqualTo(45000);
        }

        @Test
        @DisplayName("생성 직후 cancelledAt, refundedAt은 null이다")
        void noTimestampsInitially() {
            Payment payment = createPayment();

            assertThat(payment.getCancelledAt()).isNull();
            assertThat(payment.getRefundedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("결제 상태 전이")
    class StatusTransition {

        @Test
        @DisplayName("cancel() → CANCELLED 상태로 전이하고 cancelledAt이 설정된다")
        void cancel() {
            Payment payment = createPayment();

            payment.cancel();

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(payment.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("refund() → REFUNDED 상태로 전이하고 refundedAt이 설정된다")
        void refund() {
            Payment payment = createPayment();

            payment.refund();

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(payment.getRefundedAt()).isNotNull();
        }
    }
}
