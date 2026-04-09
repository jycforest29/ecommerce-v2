package ecommerce.platform.order.service;

import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.order.dto.OrderQueryResponse;
import ecommerce.platform.order.entity.Order;
import ecommerce.platform.order.entity.OrderItem;
import ecommerce.platform.order.repository.OrderItemRepository;
import ecommerce.platform.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public Page<OrderQueryResponse> queryOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByUserId(userId, pageable);

        List<Long> orderIds = orders.getContent().stream().map(Order::getId).toList();
        Map<Long, List<OrderItem>> orderItemMap = orderItemRepository.findByOrderIdIn(orderIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        return orders.map(order -> OrderQueryResponse.from(order, orderItemMap.getOrDefault(order.getId(), List.of())));
    }

    public OrderQueryResponse queryOrder(Long userId, Long orderId) {
        Order order = EntityFinder.findEntity(orderRepository, orderId);
        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException();
        }
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return OrderQueryResponse.from(order, orderItems);
    }
}