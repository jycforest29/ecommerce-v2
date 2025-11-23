package ecommerce.platform.order.service;

import ecommerce.platform.common.event.coupon.CouponApplyRequestEvent;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.common.util.OutboxEventGenerator;
import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.order.dto.OrderCreateRequest;
import ecommerce.platform.order.dto.OrderCreateResponse;
import ecommerce.platform.order.entity.Order;
import ecommerce.platform.order.entity.OrderItem;
import ecommerce.platform.order.repository.OrderItemRepository;
import ecommerce.platform.order.repository.OrderRepository;
import ecommerce.platform.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderManageService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public OrderCreateResponse createOrder(Long userId, OrderCreateRequest orderCreateRequest) {
        Order order = Order.builder()
                .userId(userId)
                .totalQuantity(orderCreateRequest.getQuantity())
                .totalPriceSnapshot(orderCreateRequest.getPriceSnapshot())
                .build();
        orderRepository.save(order);

        List<OrderItem> orderItems = orderCreateRequest.orderItemRequests()
                .stream()
                .map(orderItemRequest -> orderItemRequest.toEntity(order))
                .toList();
        orderItemRepository.saveAll(orderItems);

        CouponApplyRequestEvent couponApplyRequestEvent = CouponApplyRequestEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .orderItemInfos(orderItems.stream()
                        .map(item -> new CouponApplyRequestEvent.OrderItemInfo(
                                item.getProductId(),
                                item.getProductOptionId(),
                                item.getPriceSnapshot(),
                                item.getQuantity()))
                        .toList())
                .build();

        OutboxEvent outboxEvent = OutboxEventGenerator.publish(couponApplyRequestEvent);
        outboxEventRepository.save(outboxEvent);

        return OrderCreateResponse.from(order, orderItems);
    }

    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = EntityFinder.findEntity(orderRepository, orderId);
    }
}