package ecommerce.platform.coupon.service;

import ecommerce.platform.common.event.product.StockDeductRequestEvent;
import ecommerce.platform.common.event.review.ReviewCreatedEvent;
import ecommerce.platform.common.event.review.ReviewDeletedEvent;
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
class ProductEventHandlerTest {

    @InjectMocks
    private ProductEventHandler productEventHandler;

    @Mock
    private ProductManageService productCommandService;

    @Nested
    @DisplayName("재고 차감 요청 이벤트")
    class StockDeductRequest {

        @Test
        @DisplayName("StockDeductRequestEvent를 처리하면 deductStock이 호출된다")
        void handleStockDeductRequestEvent() {
            List<StockDeductRequestEvent.StockInfo> stockInfos = List.of(
                    new StockDeductRequestEvent.StockInfo(1L, 10L, 3)
            );
            StockDeductRequestEvent event = StockDeductRequestEvent.builder()
                    .orderId(100L)
                    .stockInfos(stockInfos)
                    .build();

            productEventHandler.handle(event);

            then(productCommandService).should().deductStock(100L, stockInfos);
        }
    }

    @Nested
    @DisplayName("리뷰 이벤트")
    class ReviewEvent {

        @Test
        @DisplayName("ReviewCreatedEvent를 처리하면 리뷰 수가 1 증가한다")
        void handleReviewCreatedEvent() {
            ReviewCreatedEvent event = ReviewCreatedEvent.builder()
                    .productId(1L)
                    .averageScore(45)
                    .build();

            productEventHandler.handle(event);

            then(productCommandService).should().updateReviewCount(1L, 1);
        }

        @Test
        @DisplayName("ReviewDeletedEvent를 처리하면 리뷰 수가 1 감소한다")
        void handleReviewDeletedEvent() {
            ReviewDeletedEvent event = ReviewDeletedEvent.builder()
                    .productId(1L)
                    .averageScore(40)
                    .build();

            productEventHandler.handle(event);

            then(productCommandService).should().updateReviewCount(1L, -1);
        }
    }
}
