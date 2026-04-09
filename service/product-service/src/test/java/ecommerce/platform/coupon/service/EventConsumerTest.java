package ecommerce.platform.coupon.service;

import ecommerce.platform.common.event.product.StockDeductRequestEvent;
import ecommerce.platform.common.event.review.ReviewCreatedEvent;
import ecommerce.platform.common.event.review.ReviewDeletedEvent;
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
    private ProductEventHandler productEventHandler;

    @Mock
    private Acknowledgment acknowledgment;

    @Test
    @DisplayName("StockDeductRequestEvent를 수신하면 핸들러에 위임하고 ack한다")
    void handleStockDeductRequestEvent() {
        StockDeductRequestEvent event = StockDeductRequestEvent.builder()
                .orderId(1L)
                .stockInfos(List.of(new StockDeductRequestEvent.StockInfo(1L, 10L, 3)))
                .build();

        eventConsumer.handleEvent(event, acknowledgment);

        then(productEventHandler).should().handle(event);
        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("ReviewCreatedEvent를 수신하면 핸들러에 위임하고 ack한다")
    void handleReviewCreatedEvent() {
        ReviewCreatedEvent event = ReviewCreatedEvent.builder()
                .productId(1L)
                .averageScore(45)
                .build();

        eventConsumer.handleEvent(event, acknowledgment);

        then(productEventHandler).should().handle(event);
        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("ReviewDeletedEvent를 수신하면 핸들러에 위임하고 ack한다")
    void handleReviewDeletedEvent() {
        ReviewDeletedEvent event = ReviewDeletedEvent.builder()
                .productId(1L)
                .averageScore(40)
                .build();

        eventConsumer.handleEvent(event, acknowledgment);

        then(productEventHandler).should().handle(event);
        then(acknowledgment).should().acknowledge();
    }
}