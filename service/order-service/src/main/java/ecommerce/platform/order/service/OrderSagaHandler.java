package ecommerce.platform.order.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.coupon.CouponAppliedEvent;
import ecommerce.platform.common.event.coupon.CouponRollbackApplyEvent;
import ecommerce.platform.common.event.payment.PaymentCompletedEvent;
import ecommerce.platform.common.event.payment.PaymentFailedEvent;
import ecommerce.platform.common.event.payment.PaymentRequestEvent;
import ecommerce.platform.common.event.product.StockDeductRequestEvent;
import ecommerce.platform.common.event.product.StockDeductedEvent;
import ecommerce.platform.common.event.product.StockRestoreRequestEvent;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.common.util.OutboxEventGenerator;
import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.order.entity.Order;
import ecommerce.platform.order.entity.OrderItem;
import ecommerce.platform.order.repository.OrderItemRepository;
import ecommerce.platform.order.repository.OrderRepository;
import ecommerce.platform.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaHandler {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void handle(Event event) {
        if (event instanceof CouponAppliedEvent e) {
            handleCouponApplied(e);
        } else if (event instanceof StockDeductedEvent e) {
            handleStockDeducted(e);
        } else if (event instanceof PaymentCompletedEvent e) {
            handlePaymentCompleted(e);
        } else if (event instanceof PaymentFailedEvent e) {
            handlePaymentFailed(e);
        }
    }

    private void handleCouponApplied(CouponAppliedEvent event) {
        Order order = findOrder(event.getOrderId());
        order.applyCoupon(event.getCouponId(), event.getDiscountRate(), event.getDiscountAmount());

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        applyCouponToOrderItems(orderItems, event.getOrderItemInfos());

        publishEvent(StockDeductRequestEvent.builder()
                .orderId(order.getId())
                .stockInfos(orderItems.stream()
                        .map(item -> new StockDeductRequestEvent.StockInfo(
                                item.getProductId(),
                                item.getProductOptionId(),
                                item.getQuantity()))
                        .toList())
                .build());
    }

    private void handleStockDeducted(StockDeductedEvent event) {
        boolean allAvailable = event.getStockInfos().stream()
                .allMatch(StockDeductedEvent.StockInfo::isAvailable);

        if (!allAvailable) {
            handleStockDeductFailed(event);
            return;
        }

        Order order = findOrder(event.getOrderId());
        order.markStockDeducted();

        publishEvent(PaymentRequestEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .paymentMethod(order.getPaymentMethod())
                .discountPrice(order.getDiscountAmount())
                .totalPrice(order.getTotalPriceSnapshot())
                .build());
    }

    private void handleStockDeductFailed(StockDeductedEvent event) {
        Order order = findOrder(event.getOrderId());
        log.warn("재고 차감 실패 - orderId: {}", order.getId());
        order.cancel();

        List<StockDeductedEvent.StockInfo> deductedItems = event.getStockInfos().stream()
                .filter(StockDeductedEvent.StockInfo::isAvailable)
                .toList();

        if (!deductedItems.isEmpty()) {
            publishEvent(StockRestoreRequestEvent.builder()
                    .orderId(order.getId())
                    .stockInfos(deductedItems.stream()
                            .map(info -> new StockDeductRequestEvent.StockInfo(
                                    info.getProductId(),
                                    info.getOptionId(),
                                    0))
                            .toList())
                    .build());
        }

        publishEvent(CouponRollbackApplyEvent.builder()
                .orderId(order.getId())
                .build());
    }

    private void handlePaymentFailed(PaymentFailedEvent event) {
        Order order = findOrder(event.getOrderId());
        log.warn("결제 실패 - orderId: {}, reason: {}", order.getId(), event.getReason());
        order.cancel();

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

        publishEvent(StockRestoreRequestEvent.builder()
                .orderId(order.getId())
                .stockInfos(orderItems.stream()
                        .map(item -> new StockDeductRequestEvent.StockInfo(
                                item.getProductId(),
                                item.getProductOptionId(),
                                item.getQuantity()))
                        .toList())
                .build());

        publishEvent(CouponRollbackApplyEvent.builder()
                .orderId(order.getId())
                .build());
    }

    private void handlePaymentCompleted(PaymentCompletedEvent event) {
        Order order = findOrder(event.getOrderId());
        order.completePayment(event.getPaymentId(), event.getPaymentMethod());
    }

    private void applyCouponToOrderItems(List<OrderItem> orderItems, List<CouponAppliedEvent.OrderItemInfo> infos) {
        for (OrderItem orderItem : orderItems) {
            for (CouponAppliedEvent.OrderItemInfo info : infos) {
                if (info.matches(orderItem.getProductId(), orderItem.getProductOptionId())) {
                    orderItem.applyCoupon(
                            info.getCouponId(),
                            info.getDiscountRate(),
                            info.getPriceBeforeCouponApplied() - info.getPriceAfterCouponApplied());
                    break;
                }
            }
        }
    }

    private Order findOrder(Long orderId) {
        return EntityFinder.findEntity(orderRepository, orderId);
    }

    private void publishEvent(Event event) {
        OutboxEvent outboxEvent = OutboxEventGenerator.publish(event);
        outboxEventRepository.save(outboxEvent);
    }
}