package ecommerce.platform.order.service;

import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.order.dto.OrderCreateRequest;
import ecommerce.platform.order.dto.OrderCreateResponse;
import ecommerce.platform.order.dto.OrderItemRequest;
import ecommerce.platform.order.entity.Order;
import ecommerce.platform.order.entity.OrderItem;
import ecommerce.platform.order.entity.OrderStatus;
import ecommerce.platform.order.repository.OrderItemRepository;
import ecommerce.platform.order.repository.OrderRepository;
import ecommerce.platform.order.repository.OutboxEventRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;;

@ExtendWith(MockitoExtension.class)
class OrderManageServiceTest {

    @InjectMocks
    private OrderManageService orderManageService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Nested
    @DisplayName("주문 생성 - createOrder")
    class CreateOrder {

        @Test
        @DisplayName("주문과 주문 아이템을 저장하고 CouponApplyRequestEvent를 Outbox에 저장한다")
        void createOrderSuccess() {
            OrderCreateRequest request = new OrderCreateRequest(
                    PaymentMethod.CREDIT_CARD,
                    List.of(
                            new OrderItemRequest(1L, 1L, 2, 10000),
                            new OrderItemRequest(2L, 3L, 1, 20000)
                    )
            );

            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(orderItemRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

            OrderCreateResponse response = orderManageService.createOrder(1L, request);

            assertThat(response.userId()).isEqualTo(1L);
            assertThat(response.totalQuantity()).isEqualTo(3);
            assertThat(response.totalPriceSnapshot()).isEqualTo(30000);
            assertThat(response.orderItemResponses()).hasSize(2);

            verify(orderRepository).save(any(Order.class));
            verify(orderItemRepository).saveAll(anyList());
            verify(outboxEventRepository).save(any(OutboxEvent.class));
        }

        @Test
        @DisplayName("생성된 주문은 CREATED 상태이다")
        void orderStatusIsCreated() {
            OrderCreateRequest request = new OrderCreateRequest(
                    PaymentMethod.MOBILE_PAYMENT,
                    List.of(new OrderItemRequest(1L, 1L, 1, 15000))
            );

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            given(orderRepository.save(orderCaptor.capture())).willAnswer(invocation -> invocation.getArgument(0));
            given(orderItemRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

            orderManageService.createOrder(1L, request);

            Order savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(savedOrder.getPaymentMethod()).isEqualTo(PaymentMethod.MOBILE_PAYMENT);
        }

        @Test
        @DisplayName("OutboxEvent에 CouponApplyRequestEvent가 직렬화되어 저장된다")
        void outboxEventContainsCouponApplyRequest() {
            OrderCreateRequest request = new OrderCreateRequest(
                    PaymentMethod.CREDIT_CARD,
                    List.of(new OrderItemRequest(1L, 1L, 1, 10000))
            );

            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(orderItemRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);

            orderManageService.createOrder(1L, request);

            verify(outboxEventRepository).save(outboxCaptor.capture());
            OutboxEvent outboxEvent = outboxCaptor.getValue();
            assertThat(outboxEvent.getEntityName()).isEqualTo("coupon.events.apply_request");
            assertThat(outboxEvent.getPayload()).contains("\"userId\":1");
        }
    }

    @Nested
    @DisplayName("주문 취소 - cancelOrder")
    class CancelOrder {

        private Order createOrder(Long id, Long userId) {
            Order order = Order.builder()
                    .userId(userId)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .totalQuantity(2)
                    .totalPriceSnapshot(30000)
                    .build();
            ReflectionTestUtils.setField(order, "id", id);
            return order;
        }

        private OrderItem createOrderItem(Order order, Long productId, Long optionId) {
            return OrderItem.builder()
                    .order(order)
                    .productId(productId)
                    .productOptionId(optionId)
                    .quantity(1)
                    .priceSnapshot(15000)
                    .build();
        }

        @Test
        @DisplayName("CREATED 상태에서 취소하면 CANCELLED로 전이하고 보상 이벤트를 발행하지 않는다")
        void cancelFromCreated() {
            Order order = createOrder(1L, 1L);
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            orderManageService.cancelOrder(1L, 1L);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
            verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
        }

        @Test
        @DisplayName("COUPON_APPLIED 상태에서 취소하면 쿠폰 롤백 이벤트만 발행한다")
        void cancelFromCouponApplied() {
            Order order = createOrder(1L, 1L);
            order.applyCoupon(5L, 10, 3000);
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            orderManageService.cancelOrder(1L, 1L);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository, times(1)).save(captor.capture());
            assertThat(captor.getValue().getEntityName()).isEqualTo("coupon.events.rollback_apply");
        }

        @Test
        @DisplayName("STOCK_DEDUCTED 상태에서 취소하면 재고 복구 + 쿠폰 롤백 이벤트를 발행한다")
        void cancelFromStockDeducted() {
            Order order = createOrder(1L, 1L);
            order.applyCoupon(5L, 10, 3000);
            order.markStockDeducted();

            OrderItem item = createOrderItem(order, 10L, 1L);

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(orderItemRepository.findByOrderId(1L)).willReturn(List.of(item));

            orderManageService.cancelOrder(1L, 1L);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository, times(2)).save(captor.capture());

            List<String> topics = captor.getAllValues().stream()
                    .map(OutboxEvent::getEntityName)
                    .toList();
            assertThat(topics).contains("stock.events.restore_request", "coupon.events.rollback_apply");
        }

        @Test
        @DisplayName("PAID 상태이면 취소할 수 없다")
        void cannotCancelPaidOrder() {
            Order order = createOrder(1L, 1L);
            order.completePayment(100L, PaymentMethod.CREDIT_CARD);
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderManageService.cancelOrder(1L, 1L))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("본인의 주문이 아니면 취소할 수 없다")
        void cannotCancelOtherUserOrder() {
            Order order = createOrder(1L, 1L);
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderManageService.cancelOrder(999L, 1L))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }
}