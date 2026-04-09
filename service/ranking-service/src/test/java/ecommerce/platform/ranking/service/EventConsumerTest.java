package ecommerce.platform.ranking.service;

import ecommerce.platform.common.constants.Category;
import ecommerce.platform.common.event.order.OrderCancelledEvent;
import ecommerce.platform.common.event.order.OrderCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EventConsumerTest {

    @InjectMocks
    private EventConsumer eventConsumer;

    @Mock
    private RankingManageService rankingManageService;

    private OrderCreatedEvent.OrderItemInfo createOrderItemInfo(Long productId, String name, Long imageId, Category category) {
        return new OrderCreatedEvent.OrderItemInfo(productId, 10L, name, imageId, category);
    }

    @Nested
    @DisplayName("OrderCreatedEvent 수신")
    class HandleOrderCreated {

        @Test
        @DisplayName("주문 생성 이벤트를 수신하면 각 아이템의 랭킹을 증가시킨다")
        void handleSingleItem() {
            OrderCreatedEvent event = OrderCreatedEvent.builder()
                    .orderId(1L)
                    .userId(1L)
                    .orderItemInfos(List.of(
                            createOrderItemInfo(1L, "상품A", 100L, Category.OUTER)
                    ))
                    .build();

            eventConsumer.handleOrderCreatedEvent(event);

            then(rankingManageService).should().updateRanking(Category.OUTER, 1L, "상품A", 100L, true);
        }

        @Test
        @DisplayName("여러 아이템이 포함된 주문이면 각각 랭킹을 증가시킨다")
        void handleMultipleItems() {
            OrderCreatedEvent event = OrderCreatedEvent.builder()
                    .orderId(1L)
                    .userId(1L)
                    .orderItemInfos(List.of(
                            createOrderItemInfo(1L, "상품A", 100L, Category.OUTER),
                            createOrderItemInfo(2L, "상품B", 200L, Category.SHOES)
                    ))
                    .build();

            eventConsumer.handleOrderCreatedEvent(event);

            then(rankingManageService).should().updateRanking(Category.OUTER, 1L, "상품A", 100L, true);
            then(rankingManageService).should().updateRanking(Category.SHOES, 2L, "상품B", 200L, true);
        }
    }

    @Nested
    @DisplayName("OrderCancelledEvent 수신")
    class HandleOrderCancelled {

        @Test
        @DisplayName("주문 취소 이벤트를 수신하면 각 아이템의 랭킹을 감소시킨다")
        void handleCancelled() {
            OrderCancelledEvent event = OrderCancelledEvent.builder()
                    .orderId(1L)
                    .orderItemInfos(List.of(
                            createOrderItemInfo(1L, "상품A", 100L, Category.OUTER)
                    ))
                    .build();

            eventConsumer.handleOrderCancelledEvent(event);

            then(rankingManageService).should().updateRanking(Category.OUTER, 1L, "상품A", 100L, false);
        }
    }
}
