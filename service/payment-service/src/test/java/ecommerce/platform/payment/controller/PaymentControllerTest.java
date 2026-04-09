package ecommerce.platform.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecommerce.platform.common.annotations.LoginUserArgumentResolver;
import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.GlobalExceptionHandler;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.payment.dto.PaymentCreateRequest;
import ecommerce.platform.payment.dto.PaymentCreateResponse;
import ecommerce.platform.payment.dto.PaymentQueryResponse;
import ecommerce.platform.payment.entity.PaymentStatus;
import ecommerce.platform.payment.service.PaymentManageService;
import ecommerce.platform.payment.service.PaymentQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private PaymentManageService paymentManageService;

    @Mock
    private PaymentQueryService paymentQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of())
        );
    }

    @Nested
    @DisplayName("POST /api/v1/payments/{orderId}")
    class CreatePayment {

        @Test
        @DisplayName("결제 생성 성공")
        void createSuccess() throws Exception {
            PaymentCreateRequest request = new PaymentCreateRequest(PaymentMethod.CREDIT_CARD, 50000, 5000);
            PaymentCreateResponse response = new PaymentCreateResponse(1L, 1L, PaymentMethod.CREDIT_CARD, 45000, Instant.now());

            given(paymentManageService.createPayment(eq(1L), any(PaymentCreateRequest.class), eq(1L))).willReturn(response);

            mockMvc.perform(post("/api/v1/payments/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentId").value(1))
                    .andExpect(jsonPath("$.finalPrice").value(45000));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments")
    class GetPayments {

        @Test
        @DisplayName("결제 목록 조회 성공")
        void getSuccess() throws Exception {
            List<PaymentQueryResponse> responses = List.of(
                    new PaymentQueryResponse(1L, 10L, PaymentMethod.CREDIT_CARD, PaymentStatus.COMPLETED, 50000, 5000, 45000, Instant.now())
            );
            given(paymentQueryService.queryPayments(1L)).willReturn(responses);

            mockMvc.perform(get("/api/v1/payments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].paymentId").value(1))
                    .andExpect(jsonPath("$[0].paymentStatus").value("COMPLETED"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/{paymentId}")
    class GetPayment {

        @Test
        @DisplayName("단건 조회 성공")
        void getSuccess() throws Exception {
            PaymentQueryResponse response = new PaymentQueryResponse(1L, 10L, PaymentMethod.CREDIT_CARD, PaymentStatus.COMPLETED, 50000, 5000, 45000, Instant.now());
            given(paymentQueryService.queryPayment(1L, 1L)).willReturn(response);

            mockMvc.perform(get("/api/v1/payments/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentId").value(1));
        }

        @Test
        @DisplayName("다른 유저의 결제 조회 시 403 반환")
        void getForbidden() throws Exception {
            given(paymentQueryService.queryPayment(1L, 1L)).willThrow(new UnauthorizedAccessException());

            mockMvc.perform(get("/api/v1/payments/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 결제 조회 시 404 반환")
        void getNotFound() throws Exception {
            given(paymentQueryService.queryPayment(1L, 999L)).willThrow(new EntityNotFoundException());

            mockMvc.perform(get("/api/v1/payments/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/orders/{orderId}")
    class GetPaymentByOrderId {

        @Test
        @DisplayName("주문 ID로 결제 조회 성공")
        void getSuccess() throws Exception {
            PaymentQueryResponse response = new PaymentQueryResponse(1L, 10L, PaymentMethod.CREDIT_CARD, PaymentStatus.COMPLETED, 50000, 5000, 45000, Instant.now());
            given(paymentQueryService.queryPaymentByOrderId(1L, 10L)).willReturn(response);

            mockMvc.perform(get("/api/v1/payments/orders/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(10));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/payments/{paymentId}/refund")
    class Refund {

        @Test
        @DisplayName("환불 성공")
        void refundSuccess() throws Exception {
            PaymentQueryResponse response = new PaymentQueryResponse(1L, 10L, PaymentMethod.CREDIT_CARD, PaymentStatus.REFUNDED, 50000, 5000, 45000, Instant.now());
            given(paymentManageService.refund(1L, 1L)).willReturn(response);

            mockMvc.perform(post("/api/v1/payments/1/refund"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentStatus").value("REFUNDED"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/payments/{orderId}/cancel")
    class Cancel {

        @Test
        @DisplayName("취소 성공")
        void cancelSuccess() throws Exception {
            PaymentQueryResponse response = new PaymentQueryResponse(1L, 10L, PaymentMethod.CREDIT_CARD, PaymentStatus.CANCELLED, 50000, 5000, 45000, Instant.now());
            given(paymentManageService.cancel(1L, 1L)).willReturn(response);

            mockMvc.perform(post("/api/v1/payments/1/cancel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentStatus").value("CANCELLED"));
        }
    }
}