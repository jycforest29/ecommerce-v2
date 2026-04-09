package ecommerce.platform.common.event;

import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.common.event.payment.PaymentCompletedEvent;
import ecommerce.platform.common.event.coupon.CouponApplyRequestEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

    @Test
    @DisplayName("이벤트 생성 시 UUID eventId가 자동 생성된다")
    void eventIdGenerated() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(1L)
                .paymentId(1L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getEventId()).isNotBlank();
    }

    @Test
    @DisplayName("서로 다른 이벤트 인스턴스는 다른 eventId를 가진다")
    void uniqueEventIds() {
        PaymentCompletedEvent event1 = PaymentCompletedEvent.builder()
                .orderId(1L).paymentId(1L).paymentMethod(PaymentMethod.CREDIT_CARD).build();
        PaymentCompletedEvent event2 = PaymentCompletedEvent.builder()
                .orderId(1L).paymentId(1L).paymentMethod(PaymentMethod.CREDIT_CARD).build();

        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    @DisplayName("toJson()으로 이벤트를 JSON 문자열로 직렬화할 수 있다")
    void toJson() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(1L)
                .paymentId(2L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        String json = event.toJson();

        assertThat(json).startsWith("{");
        assertThat(json).endsWith("}");
        assertThat(json).contains("\"orderId\":1");
        assertThat(json).contains("\"paymentId\":2");
        assertThat(json).contains("\"paymentMethod\":\"CREDIT_CARD\"");
    }

    @Test
    @DisplayName("getTopic()은 각 이벤트의 토픽을 반환한다")
    void getTopic() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(1L).paymentId(1L).paymentMethod(PaymentMethod.CREDIT_CARD).build();

        assertThat(event.getTopic()).isEqualTo("payment.events.completed");
    }

    @Test
    @DisplayName("중첩 객체가 있는 이벤트도 JSON으로 직렬화된다")
    void toJsonWithNestedObjects() {
        CouponApplyRequestEvent event = CouponApplyRequestEvent.builder()
                .orderId(1L)
                .userId(1L)
                .orderItemInfos(List.of(
                        new CouponApplyRequestEvent.OrderItemInfo(10L, 1L, 15000, 2)
                ))
                .build();

        String json = event.toJson();

        assertThat(json).startsWith("{");
        assertThat(json).endsWith("}");
        assertThat(json).contains("\"orderId\":1");
        assertThat(json).contains("\"productId\":10");
        assertThat(json).contains("\"price\":15000");
    }
}