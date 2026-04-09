package ecommerce.platform.review.service;

import ecommerce.platform.common.constants.Category;
import ecommerce.platform.common.event.order.OrderCancelledEvent;
import ecommerce.platform.common.event.order.OrderCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EventConsumerTest {

    @InjectMocks
    private EventConsumer eventConsumer;

    @Mock
    private ReviewEventHandler reviewEventHandler;

    @Mock
    private Acknowledgment acknowledgment;

    @Test
    @DisplayName("OrderCreatedEvent를 수신하면 핸들러에 위임하고 ack한다")
    void handleOrderCreatedEvent() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(1L)
                .userId(100L)
                .orderItemInfos(List.of(
                        new OrderCreatedEvent.OrderItemInfo(1L, 10L, "상품A", 100L, Category.OUTER)
                ))
                .build();

        eventConsumer.handleEvent(event, acknowledgment);

        then(reviewEventHandler).should().handle(event);
        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("OrderCancelledEvent를 수신하면 핸들러에 위임하고 ack한다")
    void handleOrderCancelledEvent() {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(1L)
                .orderItemInfos(List.of(
                        new OrderCreatedEvent.OrderItemInfo(1L, 10L, "상품A", 100L, Category.OUTER)
                ))
                .build();

        eventConsumer.handleEvent(event, acknowledgment);

        then(reviewEventHandler).should().handle(event);
        then(acknowledgment).should().acknowledge();
    }
}