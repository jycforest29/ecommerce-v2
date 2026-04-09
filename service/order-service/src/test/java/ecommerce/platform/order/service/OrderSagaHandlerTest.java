package ecommerce.platform.order.service;

import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.common.event.coupon.CouponAppliedEvent;
import ecommerce.platform.common.event.payment.PaymentCompletedEvent;
import ecommerce.platform.common.event.payment.PaymentFailedEvent;
import ecommerce.platform.common.event.product.StockDeductedEvent;
import ecommerce.platform.order.entity.Order;
import ecommerce.platform.order.entity.OrderItem;
import ecommerce.platform.order.entity.OrderStatus;
import ecommerce.platform.order.repository.OrderItemRepository;
import ecommerce.platform.order.repository.OrderRepository;
import ecommerce.platform.order.repository.OutboxEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;;

@ExtendWith(MockitoExtension.class)
class OrderSagaHandlerTest {

    @InjectMocks
    private OrderSagaHandler orderSagaHandler;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private Order createOrder(Long id) {
        Order order = Order.builder()
                .userId(1L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .totalQuantity(2)
                .totalPriceSnapshot(30000)
                .build();
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }

    private OrderItem createOrderItem(Order order, Long productId, Long optionId) {
        OrderItem item = OrderItem.builder()
                .order(order)
                .productId(productId)
                .productOptionId(optionId)
                .quantity(1)
                .priceSnapshot(15000)
                .build();
        return item;
    }

    @Nested
    @DisplayName("CouponAppliedEvent 처리")
    class HandleCouponApplied {

        @Test
        @DisplayName("쿠폰 적용 후 COUPON_APPLIED 상태로 전이하고 StockDeductRequestEvent를 발행한다")
        void handleCouponApplied() {
            Order order = createOrder(1L);
            OrderItem item = createOrderItem(order, 10L, 1L);

            CouponAppliedEvent event = CouponAppliedEvent.builder()
                    .orderId(1L)
                    .couponId(5L)
                    .discountRate(10)
                    .discountAmount(3000)
                    .orderItemInfos(List.of(
                            new CouponAppliedEvent.OrderItemInfo(10L, 1L, 5L, 15000, 1, 13500, 10)
                    ))
                    .build();

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(orderItemRepository.findByOrderId(1L)).willReturn(List.of(item));

            orderSagaHandler.handle(event);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COUPON_APPLIED);
            assertThat(order.getCouponId()).isEqualTo(5L);
            assertThat(order.getDiscountRate()).isEqualTo(10);

            assertThat(item.getCouponId()).isEqualTo(5L);
            assertThat(item.getDiscountAmount()).isEqualTo(1500); // 15000 - 13500

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository).save(captor.capture());
            assertThat(captor.getValue().getEntityName()).isEqualTo("stock.events.deduct_request");
        }
    }

    @Nested
    @DisplayName("StockDeductedEvent 처리")
    class HandleStockDeducted {

        @Test
        @DisplayName("재고 차감 후 STOCK_DEDUCTED 상태로 전이하고 PaymentRequestEvent를 발행한다")
        void handleStockDeducted() {
            Order order = createOrder(1L);
            order.applyCoupon(5L, 10, 3000);

            StockDeductedEvent event = StockDeductedEvent.builder()
                    .orderId(1L)
                    .stockInfos(List.of(
                            new StockDeductedEvent.StockInfo(10L, 1L, true)
                    ))
                    .build();

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            orderSagaHandler.handle(event);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.STOCK_DEDUCTED);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository).save(captor.capture());
            assertThat(captor.getValue().getEntityName()).isEqualTo("payment.events.request");
        }
    }

    @Nested
    @DisplayName("PaymentCompletedEvent 처리")
    class HandlePaymentCompleted {

        @Test
        @DisplayName("결제 완료 후 PAID 상태로 전이한다")
        void handlePaymentCompleted() {
            Order order = createOrder(1L);
            order.applyCoupon(5L, 10, 3000);
            order.markStockDeducted();

            PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                    .orderId(1L)
                    .paymentId(100L)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .build();

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            orderSagaHandler.handle(event);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getPaymentId()).isEqualTo(100L);
            assertThat(order.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("결제 완료 시 추가 이벤트를 발행하지 않는다")
        void noOutboxEventOnPaymentCompleted() {
            Order order = createOrder(1L);
            order.applyCoupon(5L, 10, 3000);
            order.markStockDeducted();

            PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                    .orderId(1L)
                    .paymentId(100L)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .build();

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            orderSagaHandler.handle(event);

            verify(outboxEventRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("StockDeductedEvent 실패 처리 (재고 부족)")
    class HandleStockDeductFailed {

        @Test
        @DisplayName("재고 차감 실패 시 CANCELLED 상태로 전이하고 쿠폰 롤백 이벤트를 발행한다")
        void handleStockDeductFailed() {
            Order order = createOrder(1L);
            order.applyCoupon(5L, 10, 3000);

            StockDeductedEvent event = StockDeductedEvent.builder()
                    .orderId(1L)
                    .stockInfos(List.of(
                            new StockDeductedEvent.StockInfo(10L, 1L, true),
                            new StockDeductedEvent.StockInfo(20L, 2L, false)
                    ))
                    .build();

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            orderSagaHandler.handle(event);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository, times(2)).save(captor.capture());

            List<String> topics = captor.getAllValues().stream()
                    .map(OutboxEvent::getEntityName)
                    .toList();
            assertThat(topics).contains("stock.events.restore_request", "coupon.events.rollback_apply");
        }

        @Test
        @DisplayName("모든 재고가 실패하면 재고 복구 이벤트 없이 쿠폰 롤백만 발행한다")
        void handleAllStockFailed() {
            Order order = createOrder(1L);
            order.applyCoupon(5L, 10, 3000);

            StockDeductedEvent event = StockDeductedEvent.builder()
                    .orderId(1L)
                    .stockInfos(List.of(
                            new StockDeductedEvent.StockInfo(10L, 1L, false)
                    ))
                    .build();

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            orderSagaHandler.handle(event);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository, times(1)).save(captor.capture());
            assertThat(captor.getValue().getEntityName()).isEqualTo("coupon.events.rollback_apply");
        }
    }

    @Nested
    @DisplayName("PaymentFailedEvent 처리 (결제 실패)")
    class HandlePaymentFailed {

        @Test
        @DisplayName("결제 실패 시 CANCELLED 상태로 전이하고 재고 복구 + 쿠폰 롤백 이벤트를 발행한다")
        void handlePaymentFailed() {
            Order order = createOrder(1L);
            order.applyCoupon(5L, 10, 3000);
            order.markStockDeducted();

            OrderItem item1 = createOrderItem(order, 10L, 1L);
            OrderItem item2 = createOrderItem(order, 20L, 2L);

            PaymentFailedEvent event = PaymentFailedEvent.builder()
                    .orderId(1L)
                    .reason("잔액 부족")
                    .build();

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(orderItemRepository.findByOrderId(1L)).willReturn(List.of(item1, item2));

            orderSagaHandler.handle(event);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository, times(2)).save(captor.capture());

            List<String> topics = captor.getAllValues().stream()
                    .map(OutboxEvent::getEntityName)
                    .toList();
            assertThat(topics).containsExactly("stock.events.restore_request", "coupon.events.rollback_apply");
        }
    }

    @Test
    @DisplayName("알 수 없는 이벤트는 무시한다")
    void unknownEventIgnored() {
        orderSagaHandler.handle(new ecommerce.platform.common.event.Event() {
            @Override
            public String getTopic() {
                return "unknown";
            }
        });

        verify(orderRepository, never()).findById(any());
    }
}