package ecommerce.platform.coupon.service;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.coupon.dto.CouponQueryResponse;
import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.entity.Promotion;
import ecommerce.platform.coupon.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CouponQueryServiceTest {

    @InjectMocks
    private CouponQueryService couponQueryService;

    @Mock
    private CouponRepository couponRepository;

    private Promotion createPromotion() {
        Promotion promotion = Promotion.builder()
                .promotionName("TEST_PROMO")
                .quantity(100)
                .expireDays(30)
                .discountRate(10)
                .randomDiscount(false)
                .minDiscountRate(0)
                .maxDiscountRate(0)
                .minPurchaseAmount(10000)
                .maxDiscountAmount(5000)
                .startedAt(Instant.now())
                .endedAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .category(Category.OUTER)
                .brand(Brand.A)
                .build();
        ReflectionTestUtils.setField(promotion, "promotionId", 1L);
        return promotion;
    }

    @Nested
    @DisplayName("유저 쿠폰 전체 조회 - getAllIssuedCoupons")
    class GetAllIssuedCoupons {

        @Test
        @DisplayName("유저의 쿠폰 목록을 반환한다")
        void returnsCoupons() {
            Promotion promotion = createPromotion();
            Coupon coupon1 = Coupon.of(promotion, 1L, 10);
            Coupon coupon2 = Coupon.of(promotion, 1L, 15);
            given(couponRepository.findAllByUserId(1L)).willReturn(List.of(coupon1, coupon2));

            List<CouponQueryResponse> result = couponQueryService.getAllIssuedCoupons(1L);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).discountRate()).isEqualTo(10);
            assertThat(result.get(1).discountRate()).isEqualTo(15);
        }

        @Test
        @DisplayName("쿠폰이 없으면 빈 리스트를 반환한다")
        void returnsEmpty() {
            given(couponRepository.findAllByUserId(1L)).willReturn(List.of());

            List<CouponQueryResponse> result = couponQueryService.getAllIssuedCoupons(1L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("유저 쿠폰 단건 조회 - getIssuedCoupon")
    class GetIssuedCoupon {

        @Test
        @DisplayName("본인 쿠폰을 조회한다")
        void returnsCoupon() {
            Promotion promotion = createPromotion();
            Coupon coupon = Coupon.of(promotion, 1L, 10);
            ReflectionTestUtils.setField(coupon, "couponId", 1L);
            given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));

            CouponQueryResponse result = couponQueryService.getIssuedCoupon(1L, 1L);

            assertThat(result.discountRate()).isEqualTo(10);
            assertThat(result.promotionName()).isEqualTo("TEST_PROMO");
        }

        @Test
        @DisplayName("다른 유저의 쿠폰을 조회하면 UnauthorizedAccessException 발생")
        void failDifferentUser() {
            Promotion promotion = createPromotion();
            Coupon coupon = Coupon.of(promotion, 2L, 10);
            ReflectionTestUtils.setField(coupon, "couponId", 1L);
            given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));

            assertThatThrownBy(() -> couponQueryService.getIssuedCoupon(1L, 1L))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰 조회 시 EntityNotFoundException 발생")
        void failNotFound() {
            given(couponRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> couponQueryService.getIssuedCoupon(1L, 999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
