package ecommerce.platform.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecommerce.platform.common.annotations.LoginUserArgumentResolver;
import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.GlobalExceptionHandler;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.coupon.dto.CouponApplyRequest;
import ecommerce.platform.coupon.dto.CouponApplyResponse;
import ecommerce.platform.coupon.dto.CouponIssueResponse;
import ecommerce.platform.coupon.dto.CouponQueryResponse;
import ecommerce.platform.coupon.entity.CouponStatus;
import ecommerce.platform.coupon.entity.CouponTargetItem;
import ecommerce.platform.coupon.exception.CouponFailedToApplyException;
import ecommerce.platform.coupon.exception.CouponSoldOutException;
import ecommerce.platform.coupon.service.CouponApplyService;
import ecommerce.platform.coupon.service.CouponIssueService;
import ecommerce.platform.coupon.service.CouponQueryService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CouponControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private CouponController couponController;

    @Mock
    private CouponApplyService couponApplyService;

    @Mock
    private CouponIssueService couponIssueService;

    @Mock
    private CouponQueryService couponQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(couponController)
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of())
        );
    }

    @Nested
    @DisplayName("POST /api/v1/coupons/issue/{promotionId}")
    class IssueCoupon {

        @Test
        @DisplayName("쿠폰 발급 성공 시 201 반환")
        void issueSuccess() throws Exception {
            CouponIssueResponse response = CouponIssueResponse.builder()
                    .couponId(1L)
                    .promotionName("TEST_PROMO")
                    .discountRate(10)
                    .createdAt(Instant.now())
                    .expiredAt(Instant.now().plus(30, ChronoUnit.DAYS))
                    .build();

            given(couponIssueService.issuePromotion(1L, 1L)).willReturn(response);

            mockMvc.perform(post("/api/v1/coupons/issue/1"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.couponId").value(1))
                    .andExpect(jsonPath("$.promotionName").value("TEST_PROMO"))
                    .andExpect(jsonPath("$.discountRate").value(10));
        }

        @Test
        @DisplayName("재고 소진 시 500 반환")
        void issueFail_soldOut() throws Exception {
            given(couponIssueService.issuePromotion(1L, 1L)).willThrow(new CouponSoldOutException());

            mockMvc.perform(post("/api/v1/coupons/issue/1"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/coupons")
    class GetCoupons {

        @Test
        @DisplayName("유저 쿠폰 목록 조회 성공")
        void getCouponsSuccess() throws Exception {
            List<CouponQueryResponse> responses = List.of(
                    CouponQueryResponse.builder()
                            .couponId(1L)
                            .promotionName("PROMO_1")
                            .discountRate(10)
                            .minPurchaseAmount(10000)
                            .maxDiscountAmount(5000)
                            .category(Category.OUTER)
                            .brand(Brand.A)
                            .couponStatus(CouponStatus.ISSUED)
                            .createdAt(Instant.now())
                            .expiredAt(Instant.now().plus(30, ChronoUnit.DAYS))
                            .build()
            );

            given(couponQueryService.getAllIssuedCoupons(1L)).willReturn(responses);

            mockMvc.perform(get("/api/v1/coupons"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].couponId").value(1))
                    .andExpect(jsonPath("$[0].couponStatus").value("ISSUED"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/coupons/{couponId}")
    class GetCoupon {

        @Test
        @DisplayName("단건 조회 성공")
        void getCouponSuccess() throws Exception {
            CouponQueryResponse response = CouponQueryResponse.builder()
                    .couponId(1L)
                    .promotionName("PROMO_1")
                    .discountRate(10)
                    .minPurchaseAmount(10000)
                    .maxDiscountAmount(5000)
                    .category(Category.OUTER)
                    .brand(Brand.A)
                    .couponStatus(CouponStatus.ISSUED)
                    .createdAt(Instant.now())
                    .expiredAt(Instant.now().plus(30, ChronoUnit.DAYS))
                    .build();

            given(couponQueryService.getIssuedCoupon(1L, 1L)).willReturn(response);

            mockMvc.perform(get("/api/v1/coupons/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.couponId").value(1));
        }

        @Test
        @DisplayName("다른 유저의 쿠폰 조회 시 403 반환")
        void getCouponForbidden() throws Exception {
            given(couponQueryService.getIssuedCoupon(1L, 1L)).willThrow(new UnauthorizedAccessException());

            mockMvc.perform(get("/api/v1/coupons/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰 조회 시 404 반환")
        void getCouponNotFound() throws Exception {
            given(couponQueryService.getIssuedCoupon(1L, 999L)).willThrow(new EntityNotFoundException());

            mockMvc.perform(get("/api/v1/coupons/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/coupons/{couponId}/apply")
    class ApplyCoupon {

        @Test
        @DisplayName("쿠폰 적용 성공")
        void applySuccess() throws Exception {
            CouponApplyRequest request = new CouponApplyRequest(List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(20000).quantity(1).build()
            ));

            CouponApplyResponse response = CouponApplyResponse.from(Instant.now(), 20000, 2000);

            given(couponApplyService.apply(eq(1L), eq(1L), any(CouponApplyRequest.class))).willReturn(response);

            mockMvc.perform(patch("/api/v1/coupons/1/apply")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.originalPrice").value(20000))
                    .andExpect(jsonPath("$.discountAmount").value(2000))
                    .andExpect(jsonPath("$.finalPrice").value(18000));
        }

        @Test
        @DisplayName("적용 실패 시 500 반환")
        void applyFail() throws Exception {
            CouponApplyRequest request = new CouponApplyRequest(List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(20000).quantity(1).build()
            ));

            given(couponApplyService.apply(eq(1L), eq(1L), any(CouponApplyRequest.class)))
                    .willThrow(new CouponFailedToApplyException());

            mockMvc.perform(patch("/api/v1/coupons/1/apply")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/coupons/{couponId}/apply/rollback")
    class RollbackApply {

        @Test
        @DisplayName("롤백 성공 시 200 반환")
        void rollbackSuccess() throws Exception {
            mockMvc.perform(patch("/api/v1/coupons/1/apply/rollback"))
                    .andExpect(status().isOk());
        }
    }
}
