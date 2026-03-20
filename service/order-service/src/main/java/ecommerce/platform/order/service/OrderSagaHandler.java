package ecommerce.platform.order.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.coupon.CouponAppliedEvent;
import ecommerce.platform.common.event.payment.PaymentCompletedEvent;
import ecommerce.platform.common.event.payment.PaymentRequestEvent;
import ecommerce.platform.common.event.product.StockDeductRequestEvent;
import ecommerce.platform.common.event.product.StockDeductedEvent;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.common.util.OutboxEventGenerator;
import ecommerce.platform.common.event.OutboxEvent;
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