package ecommerce.platform.review.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.order.OrderCancelledEvent;
import ecommerce.platform.common.event.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewEventHandler {

    private final PurchaseVerifier purchaseVerifier;

    public void handle(Event event) {
        if (event instanceof OrderCreatedEvent e) {
            handleOrderCreated(e);
        } else if (event instanceof OrderCancelledEvent e) {
            handleOrderCancelled(e);
        }
    }

    private void handleOrderCreated(OrderCreatedEvent event) {
        for (OrderCreatedEvent.OrderItemInfo item : event.getOrderItemInfos()) {
            purchaseVerifier.addPurchase(item.getProductId(), event.getUserId());
        }
    }

    private void handleOrderCancelled(OrderCancelledEvent event) {
        // TODO: OrderCancelledEvent에 productId/userId 추가 후 구현
    }
}