package ecommerce.platform.review.service;

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
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReviewEventHandlerTest {

    @InjectMocks
    private ReviewEventHandler reviewEventHandler;

    @Mock
    private PurchaseVerifier purchaseVerifier;

    @Nested
    @DisplayName("OrderCreatedEvent 처리")
    class HandleOrderCreated {

        @Test
        @DisplayName("주문 생성 이벤트를 수신하면 각 아이템의 구매 이력을 추가한다")
        void addPurchaseForEachItem() {
            OrderCreatedEvent event = OrderCreatedEvent.builder()
                    .orderId(1L)
                    .userId(100L)
                    .orderItemInfos(List.of(
                            new OrderCreatedEvent.OrderItemInfo(1L, 10L, "상품A", 100L, Category.OUTER),
                            new OrderCreatedEvent.OrderItemInfo(2L, 20L, "상품B", 200L, Category.SHOES)
                    ))
                    .build();

            reviewEventHandler.handle(event);

            then(purchaseVerifier).should().addPurchase(1L, 100L);
            then(purchaseVerifier).should().addPurchase(2L, 100L);
        }
    }

    @Nested
    @DisplayName("OrderCancelledEvent 처리")
    class HandleOrderCancelled {

        @Test
        @DisplayName("주문 취소 이벤트는 현재 미구현이므로 구매 이력을 변경하지 않는다")
        void cancelledEventDoesNothing() {
            OrderCancelledEvent event = OrderCancelledEvent.builder()
                    .orderId(1L)
                    .orderItemInfos(List.of(
                            new OrderCreatedEvent.OrderItemInfo(1L, 10L, "상품A", 100L, Category.OUTER)
                    ))
                    .build();

            reviewEventHandler.handle(event);

            then(purchaseVerifier).should(never()).removePurchase(1L, 100L);
        }
    }
}