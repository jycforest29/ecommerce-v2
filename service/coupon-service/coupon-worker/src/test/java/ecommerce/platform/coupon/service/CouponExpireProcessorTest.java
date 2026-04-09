package ecommerce.platform.coupon.service;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.entity.CouponLog;
import ecommerce.platform.coupon.entity.CouponStatus;
import ecommerce.platform.coupon.entity.Promotion;
import ecommerce.platform.coupon.repository.CouponLogRepository;
import ecommerce.platform.coupon.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CouponExpireProcessorTest {

    @InjectMocks
    private CouponExpireProcessor couponExpireProcessor;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponLogRepository couponLogRepository;

    private Promotion createPromotion() {
        return Promotion.builder()
                .promotionName("TEST_PROMO")
                .quantity(100)
                .expireDays(1)
                .discountRate(10)
                .randomDiscount(false)
                .minDiscountRate(0)
                .maxDiscountRate(0)
                .minPurchaseAmount(10000)
                .maxDiscountAmount(5000)
                .startedAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .endedAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .category(Category.OUTER)
                .brand(Brand.A)
                .build();
    }

    @Test
    @DisplayName("만료 대상 쿠폰을 EXPIRED 상태로 전이하고 로그를 저장한다")
    void expireCoupons() {
        Promotion promotion = createPromotion();
        Coupon coupon1 = Coupon.of(promotion, 1L, 10);
        Coupon coupon2 = Coupon.of(promotion, 2L, 15);
        ReflectionTestUtils.setField(coupon1, "expiredAt", Instant.now().minus(1, ChronoUnit.HOURS));
        ReflectionTestUtils.setField(coupon2, "expiredAt", Instant.now().minus(2, ChronoUnit.HOURS));

        given(couponRepository.findAllCouponsToExpire()).willReturn(List.of(coupon1, coupon2));

        couponExpireProcessor.expireCoupons();

        assertThat(coupon1.getCouponStatus()).isEqualTo(CouponStatus.EXPIRED);
        assertThat(coupon2.getCouponStatus()).isEqualTo(CouponStatus.EXPIRED);

        ArgumentCaptor<List<CouponLog>> captor = ArgumentCaptor.forClass(List.class);
        verify(couponLogRepository).saveAll(captor.capture());
        List<CouponLog> savedLogs = captor.getValue();
        assertThat(savedLogs).hasSize(2);
        assertThat(savedLogs).allMatch(log -> log.getCouponStatus() == CouponStatus.EXPIRED);
    }

    @Test
    @DisplayName("만료 대상이 없으면 아무 것도 저장하지 않는다")
    void expireNoCoupons() {
        given(couponRepository.findAllCouponsToExpire()).willReturn(List.of());

        couponExpireProcessor.expireCoupons();

        verify(couponLogRepository).saveAll(List.of());
    }
}