package ecommerce.platform.payment.service;

import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.payment.dto.PaymentQueryResponse;
import ecommerce.platform.payment.entity.Payment;
import ecommerce.platform.payment.repository.PaymentRepository;
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
class PaymentQueryServiceTest {

    @InjectMocks
    private PaymentQueryService paymentQueryService;

    @Mock
    private PaymentRepository paymentRepository;

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
    @DisplayName("결제 목록 조회 - queryPayments")
    class QueryPayments {

        @Test
        @DisplayName("유저의 결제 목록을 반환한다")
        void querySuccess() {
            Payment p1 = createPayment(1L, 10L, 1L);
            Payment p2 = createPayment(2L, 20L, 1L);
            given(paymentRepository.findAllByUserId(1L)).willReturn(List.of(p1, p2));

            List<PaymentQueryResponse> result = paymentQueryService.queryPayments(1L);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("결제가 없으면 빈 리스트를 반환한다")
        void queryEmpty() {
            given(paymentRepository.findAllByUserId(1L)).willReturn(List.of());

            assertThat(paymentQueryService.queryPayments(1L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("결제 단건 조회 - queryPayment")
    class QueryPayment {

        @Test
        @DisplayName("본인 결제를 조회한다")
        void querySuccess() {
            Payment payment = createPayment(1L, 10L, 1L);
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

            PaymentQueryResponse result = paymentQueryService.queryPayment(1L, 1L);

            assertThat(result.paymentId()).isEqualTo(1L);
            assertThat(result.finalPrice()).isEqualTo(45000);
        }

        @Test
        @DisplayName("다른 유저의 결제 조회 시 UnauthorizedAccessException 발생")
        void queryFail_differentUser() {
            Payment payment = createPayment(1L, 10L, 2L);
            given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentQueryService.queryPayment(1L, 1L))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }

        @Test
        @DisplayName("존재하지 않는 결제 조회 시 EntityNotFoundException 발생")
        void queryFail_notFound() {
            given(paymentRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentQueryService.queryPayment(1L, 999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("주문 ID로 결제 조회 - queryPaymentByOrderId")
    class QueryPaymentByOrderId {

        @Test
        @DisplayName("주문 ID로 본인 결제를 조회한다")
        void querySuccess() {
            Payment payment = createPayment(1L, 10L, 1L);
            given(paymentRepository.findByOrderId(10L)).willReturn(Optional.of(payment));

            PaymentQueryResponse result = paymentQueryService.queryPaymentByOrderId(1L, 10L);

            assertThat(result.orderId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("다른 유저의 결제 조회 시 UnauthorizedAccessException 발생")
        void queryFail_differentUser() {
            Payment payment = createPayment(1L, 10L, 2L);
            given(paymentRepository.findByOrderId(10L)).willReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentQueryService.queryPaymentByOrderId(1L, 10L))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }

        @Test
        @DisplayName("존재하지 않는 주문의 결제 조회 시 EntityNotFoundException 발생")
        void queryFail_notFound() {
            given(paymentRepository.findByOrderId(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentQueryService.queryPaymentByOrderId(1L, 999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
