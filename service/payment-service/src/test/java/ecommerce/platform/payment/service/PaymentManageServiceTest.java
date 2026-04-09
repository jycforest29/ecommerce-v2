package ecommerce.platform.payment.service;

import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.payment.controller.PGMockController.PGResponse;
import ecommerce.platform.payment.dto.PaymentCreateRequest;
import ecommerce.platform.payment.dto.PaymentCreateResponse;
import ecommerce.platform.payment.dto.PaymentQueryResponse;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentManageServiceTest {

    @InjectMocks
    private PaymentManageService paymentManageService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private PGClient pgClient;

    private Payment createPayment(Long id, Long orderId, Long userId) {
        Payment payment = Payment.builder()
                .orderId(orderId)
                .userId(userId)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .totalPrice(50000)
                .discountAmount(5000)
                .build();
        ReflectionTestUtils.setField(payment, "id", id);
        return payment;
    }

    @Nested
    @DisplayName("결제 생성 - createPayment")
    class CreatePayment {

        @Test
        @DisplayName("PG 승인 성공 시 결제를 생성하고 PaymentCompletedEvent를 Outbox에 저장한다")
        void createSuccess() {
            given(pgClient.requestPayment()).willReturn(PGResponse.success("결제 승인 완료"));
            given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

            PaymentCreateRequest request = new PaymentCreateRequest(PaymentMethod.CREDIT_CARD, 50000, 5000);

            PaymentCreateResponse response = paymentManageService.createPayment(1L, request, 1L);

            assertThat(response.finalPrice()).isEqualTo(45000);
            verify(paymentRepository).save(any(Payment.class));

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository).save(captor.capture());
            assertThat(captor.getValue().getEntityName()).isEqualTo("payment.events.completed");
        }

        @Test
        @DisplayName("PG 승인 실패 시 RuntimeException 발생하고 결제를 저장하지 않는다")
        void createFail_pgRejected() {
            given(pgClient.requestPayment()).willReturn(PGResponse.fail("결제 승인 실패"));

            PaymentCreateRequest request = new PaymentCreateRequest(PaymentMethod.CREDIT_CARD, 50000, 5000);

            assertThatThrownBy(() -> paymentManageService.createPayment(1L, request, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("결제 승인 실패");

            verify(paymentRepository, never()).save(any());
            verify(outboxEventRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("결제 취소 - cancel")
    class Cancel {

        @Test
        @DisplayName("본인 결제를 취소하고 PaymentCancelledEvent를 발행한다")
        void cancelSuccess() {
            Payment payment = createPayment(1L, 10L, 1L);
            given(paymentRepository.findByOrderId(10L)).willReturn(Optional.of(payment));

            PaymentQueryResponse response = paymentManageService.cancel(10L, 1L);

            assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.CANCELLED);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository).save(captor.capture());
            assertThat(captor.getValue().getEntityName()).isEqualTo("payment.events.cancelled");
        }

        @Test
        @DisplayName("다른 유저의 결제 취소 시 UnauthorizedAccessException 발생")
        void cancelFail_differentUser() {
            Payment payment = createPayment(1L, 10L, 2L);
            given(paymentRepository.findByOrderId(10L)).willReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentManageService.cancel(10L, 1L))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }

        @Test
        @DisplayName("존재하지 않는 주문의 결제 취소 시 EntityNotFoundException 발생")
        void cancelFail_notFound() {
            given(paymentRepository.findByOrderId(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentManageService.cancel(999L, 1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("환불 - refund")
    class Refund {

        @Test
        @DisplayName("PG 환불 성공 시 REFUNDED 상태로 전이하고 RefundCompletedEvent를 발행한다")
        void refundSuccess() {
            Payment payment = createPayment(1L, 10L, 1L);
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));
            given(pgClient.requestRefund()).willReturn(PGResponse.success("환불 처리 완료"));

            PaymentQueryResponse response = paymentManageService.refund(1L, 1L);

            assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.REFUNDED);

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepository).save(captor.capture());
            assertThat(captor.getValue().getEntityName()).isEqualTo("payment.events.refund_completed");
        }

        @Test
        @DisplayName("PG 환불 실패 시 RuntimeException 발생")
        void refundFail_pgRejected() {
            Payment payment = createPayment(1L, 10L, 1L);
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));
            given(pgClient.requestRefund()).willReturn(PGResponse.fail("환불 처리 실패"));

            assertThatThrownBy(() -> paymentManageService.refund(1L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("환불 처리 실패");
        }

        @Test
        @DisplayName("다른 유저의 결제 환불 시 UnauthorizedAccessException 발생")
        void refundFail_differentUser() {
            Payment payment = createPayment(1L, 10L, 2L);
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentManageService.refund(1L, 1L))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }
}
