package ecommerce.platform.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecommerce.platform.common.annotations.LoginUserArgumentResolver;
import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.GlobalExceptionHandler;
import ecommerce.platform.coupon.dto.PromotionQueryResponse;
import ecommerce.platform.coupon.dto.PromotionRegisterRequest;
import ecommerce.platform.coupon.dto.PromotionRegisterResponse;
import ecommerce.platform.coupon.service.PromotionManageService;
import ecommerce.platform.coupon.service.PromotionQueryService;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PromotionControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private PromotionController promotionController;

    @Mock
    private PromotionManageService promotionManageService;

    @Mock
    private PromotionQueryService promotionQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(promotionController)
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of())
        );
    }

    @Nested
    @DisplayName("POST /api/v1/promotions")
    class RegisterPromotion {

        @Test
        @DisplayName("프로모션 등록 성공 시 201 반환")
        void registerSuccess() throws Exception {
            PromotionRegisterRequest request = new PromotionRegisterRequest(
                    "NEW_PROMO", 100, 30, 10, false, 0, 0,
                    10000, 5000,
                    Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
                    Category.OUTER, Brand.A
            );

            PromotionRegisterResponse response = new PromotionRegisterResponse(1L, "NEW_PROMO", 100);

            given(promotionManageService.register(any(PromotionRegisterRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/v1/promotions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.promotionId").value(1))
                    .andExpect(jsonPath("$.promotionName").value("NEW_PROMO"))
                    .andExpect(jsonPath("$.quantity").value(100));
        }

        @Test
        @DisplayName("필수값 누락 시 400 반환")
        void registerFail_validation() throws Exception {
            String invalidRequest = """
                    {
                        "promotionName": "",
                        "quantity": 0,
                        "expireDays": 0,
                        "minPurchaseAmount": 0,
                        "maxDiscountAmount": 0
                    }
                    """;

            mockMvc.perform(post("/api/v1/promotions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/promotions")
    class GetAllPromotions {

        @Test
        @DisplayName("프로모션 전체 조회 성공")
        void getAllSuccess() throws Exception {
            List<PromotionQueryResponse> responses = List.of(
                    new PromotionQueryResponse(1L, "PROMO_1", 10, false, 0, 0,
                            10000, 5000, Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
                            Category.OUTER, Brand.A),
                    new PromotionQueryResponse(2L, "PROMO_2", 20, false, 0, 0,
                            20000, 10000, Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
                            Category.ALL, Brand.ALL)
            );

            given(promotionQueryService.getAllPromotions()).willReturn(responses);

            mockMvc.perform(get("/api/v1/promotions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].promotionName").value("PROMO_1"))
                    .andExpect(jsonPath("$[1].promotionName").value("PROMO_2"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/promotions/{promotionId}")
    class GetPromotion {

        @Test
        @DisplayName("프로모션 단건 조회 성공")
        void getSuccess() throws Exception {
            PromotionQueryResponse response = new PromotionQueryResponse(
                    1L, "PROMO_1", 10, false, 0, 0,
                    10000, 5000, Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
                    Category.OUTER, Brand.A
            );

            given(promotionQueryService.getPromotion(1L)).willReturn(response);

            mockMvc.perform(get("/api/v1/promotions/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.promotionId").value(1));
        }

        @Test
        @DisplayName("존재하지 않는 프로모션 조회 시 404 반환")
        void getNotFound() throws Exception {
            given(promotionQueryService.getPromotion(999L)).willThrow(new EntityNotFoundException());

            mockMvc.perform(get("/api/v1/promotions/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/promotions")
    class DeletePromotion {

        @Test
        @DisplayName("프로모션 삭제 성공 시 200 반환")
        void deleteSuccess() throws Exception {
            mockMvc.perform(delete("/api/v1/promotions")
                            .param("promotionId", "1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("존재하지 않는 프로모션 삭제 시 404 반환")
        void deleteNotFound() throws Exception {
            doThrow(new EntityNotFoundException()).when(promotionManageService).remove(999L);

            mockMvc.perform(delete("/api/v1/promotions")
                            .param("promotionId", "999"))
                    .andExpect(status().isNotFound());
        }
    }
}
