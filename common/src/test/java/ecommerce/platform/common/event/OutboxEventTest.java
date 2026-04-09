package ecommerce.platform.common.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventTest {

    @Test
    @DisplayName("OutboxEvent가 미발행 상태로 생성된다")
    void initiallyUnpublished() {
        OutboxEvent event = OutboxEvent.builder()
                .entityName("order.events.created")
                .entityId("uuid-123")
                .payload("{\"orderId\":1}")
                .build();

        assertThat(event.getEntityName()).isEqualTo("order.events.created");
        assertThat(event.getEntityId()).isEqualTo("uuid-123");
        assertThat(event.getPayload()).isEqualTo("{\"orderId\":1}");
        assertThat(event.isPublished()).isFalse();
        assertThat(event.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("markPublished()로 발행 상태로 전이한다")
    void markPublished() {
        OutboxEvent event = OutboxEvent.builder()
                .entityName("order.events.created")
                .entityId("uuid-123")
                .payload("{}")
                .build();

        event.markPublished();

        assertThat(event.isPublished()).isTrue();
    }
}
