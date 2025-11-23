package ecommerce.platform.ranking.service;

import ecommerce.platform.common.event.order.OrderCancelledEvent;
import ecommerce.platform.common.event.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankingEventConsumer {

    private final RankingQueryService rankingQueryService;

    @KafkaListener(topics = TopicConstants.ORDER_CREATED)
    public void handleOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent){
        rankingQueryService.updateRanking(orderCreatedEvent.category(), orderCreatedEvent.productId(),
                orderCreatedEvent.productName(), orderCreatedEvent.imageId(), true);
    }

    @KafkaListener(topics = TopicConstants.ORDER_CANCELLED)
    public void handleOrderCancelledEvent(OrderCancelledEvent orderCancelledEvent){
        rankingQueryService.updateRanking(orderCancelledEvent.category(), orderCancelledEvent.productId(),
                orderCancelledEvent.productName(), orderCancelledEvent.imageId(), false);
    }

}
