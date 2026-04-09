package ecommerce.platform.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecommerce.platform.common.annotations.LoginUserArgumentResolver;
import ecommerce.platform.common.constants.PaymentMethod;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.GlobalExceptionHandler;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.order.dto.*;
import ecommerce.platform.order.entity.OrderStatus;
import ecommerce.platform.order.service.OrderManageService;
import ecommerce.platform.order.service.OrderQueryService;
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
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private OrderController orderController;

    @Mock
    private OrderManageService orderManageService;

    @Mock
    private OrderQueryService orderQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of())
        );
    }

    @Nested
    @DisplayName("POST /api/v1/orders")
    class CreateOrder {

        @Test
        @DisplayName("주문 생성 성공 시 200 반환")
        void createSuccess() throws Exception {
            OrderCreateRequest request = new OrderCreateRequest(
                    PaymentMethod.CREDIT_CARD,
                    List.of(new OrderItemRequest(1L, 1L, 2, 10000))
            );

            OrderCreateResponse response = OrderCreateResponse.builder()
                    .userId(1L)
                    .totalQuantity(2)
                    .totalPriceSnapshot(10000)
                    .createdAt(Instant.now())
                    .orderItemResponses(List.of(
                            new OrderItemResponse(1L, 1L, 2, 10000, null, null, null)
                    ))
                    .build();

            given(orderManageService.createOrder(eq(1L), any(OrderCreateRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.totalQuantity").value(2));
        }

        @Test
        @DisplayName("필수값 누락 시 400 반환")
        void createFail_validation() throws Exception {
            String invalidRequest = """
                    {
                        "orderItemRequests": []
                    }
                    """;

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders")
    class QueryOrders {

        @Test
        @DisplayName("주문 목록 조회 성공")
        void querySuccess() throws Exception {
            List<OrderQueryResponse> responses = List.of(
                    OrderQueryResponse.builder()
                            .orderId(1L)
                            .userId(1L)
                            .totalPriceSnapshot(30000)
                            .totalQuantity(2)
                            .orderStatus(OrderStatus.CREATED)
                            .createdAt(Instant.now())
                            .orderItemResponses(List.of())
                            .build()
            );

            given(orderQueryService.queryOrders(1L)).willReturn(responses);

            mockMvc.perform(get("/api/v1/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].orderId").value(1))
                    .andExpect(jsonPath("$[0].orderStatus").value("CREATED"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{orderId}")
    class QueryOrder {

        @Test
        @DisplayName("단건 조회 성공")
        void querySuccess() throws Exception {
            OrderQueryResponse response = OrderQueryResponse.builder()
                    .orderId(1L)
                    .userId(1L)
                    .totalPriceSnapshot(30000)
                    .totalQuantity(2)
                    .orderStatus(OrderStatus.PAID)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .createdAt(Instant.now())
                    .paidAt(Instant.now())
                    .orderItemResponses(List.of())
                    .build();

            given(orderQueryService.queryOrder(1L, 1L)).willReturn(response);

            mockMvc.perform(get("/api/v1/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(1))
                    .andExpect(jsonPath("$.orderStatus").value("PAID"));
        }

        @Test
        @DisplayName("다른 유저의 주문 조회 시 403 반환")
        void queryForbidden() throws Exception {
            given(orderQueryService.queryOrder(1L, 1L)).willThrow(new UnauthorizedAccessException());

            mockMvc.perform(get("/api/v1/orders/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 404 반환")
        void queryNotFound() throws Exception {
            given(orderQueryService.queryOrder(1L, 999L)).willThrow(new EntityNotFoundException());

            mockMvc.perform(get("/api/v1/orders/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/orders/{orderId}")
    class CancelOrder {

        @Test
        @DisplayName("주문 취소 성공 시 200 반환")
        void cancelSuccess() throws Exception {
            mockMvc.perform(delete("/api/v1/orders/1"))
                    .andExpect(status().isOk());
        }
    }
}
