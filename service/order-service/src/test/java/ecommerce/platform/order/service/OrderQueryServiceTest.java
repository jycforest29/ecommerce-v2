package ecommerce.platform.order.service;

import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.order.dto.OrderQueryResponse;
import ecommerce.platform.order.entity.Order;
import ecommerce.platform.order.entity.OrderItem;
import ecommerce.platform.order.repository.OrderItemRepository;
import ecommerce.platform.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @InjectMocks
    private OrderQueryService orderQueryService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

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

    private OrderItem createOrderItem(Order order, Long productId) {
        return OrderItem.builder()
                .order(order)
                .productId(productId)
                .productOptionId(1L)
                .quantity(1)
                .priceSnapshot(15000)
                .build();
    }

    @Nested
    @DisplayName("주문 목록 조회 - queryOrders")
    class QueryOrders {

        @Test
        @DisplayName("유저의 주문 목록을 반환한다")
        void queryOrdersSuccess() {
            Order order1 = createOrder(1L, 1L);
            Order order2 = createOrder(2L, 1L);
            OrderItem item1 = createOrderItem(order1, 10L);
            OrderItem item2 = createOrderItem(order2, 20L);

            given(orderRepository.findAllByUserId(1L)).willReturn(List.of(order1, order2));
            given(orderItemRepository.findByOrderIdIn(List.of(1L, 2L))).willReturn(List.of(item1, item2));

            List<OrderQueryResponse> result = orderQueryService.queryOrders(1L);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("주문이 없으면 빈 리스트를 반환한다")
        void queryOrdersEmpty() {
            given(orderRepository.findAllByUserId(1L)).willReturn(List.of());

            assertThat(orderQueryService.queryOrders(1L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("주문 단건 조회 - queryOrder")
    class QueryOrder {

        @Test
        @DisplayName("본인 주문을 조회한다")
        void queryOrderSuccess() {
            Order order = createOrder(1L, 1L);
            OrderItem item = createOrderItem(order, 10L);

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(orderItemRepository.findByOrderId(1L)).willReturn(List.of(item));

            OrderQueryResponse result = orderQueryService.queryOrder(1L, 1L);

            assertThat(result.orderId()).isEqualTo(1L);
            assertThat(result.orderItemResponses()).hasSize(1);
        }

        @Test
        @DisplayName("다른 유저의 주문 조회 시 UnauthorizedAccessException 발생")
        void queryOrderFail_differentUser() {
            Order order = createOrder(1L, 2L);
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderQueryService.queryOrder(1L, 1L))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 EntityNotFoundException 발생")
        void queryOrderFail_notFound() {
            given(orderRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderQueryService.queryOrder(1L, 999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}