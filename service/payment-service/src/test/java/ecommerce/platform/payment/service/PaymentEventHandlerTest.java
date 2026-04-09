package ecommerce.platform.payment.service;

import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.common.event.order.OrderCancelledEvent;
import ecommerce.platform.common.event.payment.PaymentRequestEvent;
import ecommerce.platform.payment.controller.PGMockController.PGResponse;
import ecommerce.platform.payment.entity.Payment;
import ecommerce.platform.payment.entity.PaymentStatus;
import ecommerce.platform.payment.repository.OutboxEventRepository;
import ecommerce.platform.payment.repository.PaymentRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventHandlerTest {

    @InjectMocks
    private PaymentEventHandler paymentEventHandler;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private PGClient pgClient;

    @Nested
    @DisplayName("PaymentRequestEvent 처리")
    class HandlePaymentRequest {

        @Test
        @DisplayName("PG 승인 성공 시 결제를 생성하고 PaymentCompletedEvent를 발행한다")
        void handleSuccess() {
            PaymentRequestEvent event = PaymentRequestEvent.builder()
                    .orderId(1L)
                    .userId(1L)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .totalPrice(50000)
                    .discountPrice(5000)
                    .build();

            given(pgClient.requestPayment()).willReturn(PGResponse.success("결제 승인 완료"));
            given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

            paymentEventHandler.handle(event);

            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(paymentCaptor.capture());

            Payment saved = paymentCaptor.getValue();
            assertThat(saved.getOrderId()).isEqualTo(1L);
            assertThat(saved.getUserId()).isEqualTo(1L);
            assertThat(saved.getTotalPrice()).isEqualTo(50000);
            assertThat(saved.getDiscountAmount()).isEqualTo(5000);
            assertThat(saved.getFinalPrice()).isEqualTo(45000);

            ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository).save(outboxCaptor.capture());
            assertThat(outboxCaptor.getValue().getEntityName()).isEqualTo("payment.events.completed");
        }

        @Test
        @DisplayName("PG 승인 실패 시 RuntimeException 발생하고 결제를 저장하지 않는다")
        void handleFail_pgRejected() {
            PaymentRequestEvent event = PaymentRequestEvent.builder()
                    .orderId(1L)
                    .userId(1L)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .totalPrice(50000)
                    .discountPrice(5000)
                    .build();

            given(pgClient.requestPayment()).willReturn(PGResponse.fail("결제 승인 실패"));

            assertThatThrownBy(() -> paymentEventHandler.handle(event))
                    .isInstanceOf(RuntimeException.class);

            verify(paymentRepository, never()).save(any());
            verify(outboxEventRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("OrderCancelledEvent 처리")
    class HandleOrderCancelled {

        @Test
        @DisplayName("주문 취소 시 결제를 CANCELLED로 전이하고 PaymentCancelledEvent를 발행한다")
        void handleSuccess() {
            Payment payment = Payment.builder()
                    .orderId(1L)
                    .userId(1L)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .totalPrice(50000)
                    .discountAmount(5000)
                    .build();
            ReflectionTestUtils.setField(payment, "id", 1L);

            OrderCancelledEvent event = OrderCancelledEvent.builder()
                    .orderId(1L)
                    .orderItemInfos(List.of())
                    .build();

            given(paymentRepository.findByOrderId(1L)).willReturn(Optional.of(payment));

            paymentEventHandler.handle(event);

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository).save(captor.capture());
            assertThat(captor.getValue().getEntityName()).isEqualTo("payment.events.cancelled");
        }
    }

    @Test
    @DisplayName("알 수 없는 이벤트는 무시한다")
    void unknownEventIgnored() {
        paymentEventHandler.handle(new ecommerce.platform.common.event.Event() {
            @Override
            public String getTopic() {
                return "unknown";
            }
        });

        verify(paymentRepository, never()).save(any());
        verify(paymentRepository, never()).findByOrderId(any());
    }
}
