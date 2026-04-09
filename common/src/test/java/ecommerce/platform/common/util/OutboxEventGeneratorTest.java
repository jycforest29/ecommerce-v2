package ecommerce.platform.common.util;

import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.common.event.payment.PaymentCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventGeneratorTest {

    @Test
    @DisplayName("Event를 OutboxEvent로 변환한다")
    void publish() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(1L)
                .paymentId(2L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        OutboxEvent outboxEvent = OutboxEventGenerator.publish(event);

        assertThat(outboxEvent.getEntityName()).isEqualTo("payment.events.completed");
        assertThat(outboxEvent.getEntityId()).isEqualTo(event.getEventId());
        assertThat(outboxEvent.getPayload()).contains("\"orderId\":1");
        assertThat(outboxEvent.isPublished()).isFalse();
    }
}