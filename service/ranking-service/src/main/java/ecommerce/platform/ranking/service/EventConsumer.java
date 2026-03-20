package ecommerce.platform.ranking.service;

import ecommerce.platform.common.event.order.OrderCancelledEvent;
import ecommerce.platform.common.event.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final RankingManageService rankingManageService;

    @KafkaListener(topics = OrderCreatedEvent.TOPIC)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        event.getOrderItemInfos().forEach(item ->
                rankingManageService.updateRanking(item.getCategory(), item.getProductId(),
                        item.getProductName(), item.getImageId(), true)
        );
    }

    @KafkaListener(topics = OrderCancelledEvent.TOPIC)
    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        event.getOrderItemInfos().forEach(item ->
                rankingManageService.updateRanking(item.getCategory(), item.getProductId(),
                        item.getProductName(), item.getImageId(), false)
        );
    }
}
