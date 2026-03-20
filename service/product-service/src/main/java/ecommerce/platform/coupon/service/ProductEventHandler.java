package ecommerce.platform.coupon.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.product.StockDeductRequestEvent;
import ecommerce.platform.common.event.review.ReviewCreatedEvent;
import ecommerce.platform.common.event.review.ReviewDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductEventHandler {

    private final ProductManageService productCommandService;

    @Transactional
    public void handle(Event event) {
        if (event instanceof StockDeductRequestEvent e) {
            handleStockDeductRequest(e);
        } else if (event instanceof ReviewCreatedEvent e) {
            handleReviewCreated(e);
        } else if (event instanceof ReviewDeletedEvent e) {
            handleReviewDeleted(e);
        }
    }

    private void handleStockDeductRequest(StockDeductRequestEvent event) {
        productCommandService.deductStock(event.getOrderId(), event.getStockInfos());
    }

    private void handleReviewCreated(ReviewCreatedEvent event) {
        productCommandService.updateReviewCount(event.getProductId(), 1);
    }

    private void handleReviewDeleted(ReviewDeletedEvent event) {
        productCommandService.updateReviewCount(event.getProductId(), -1);
    }
}