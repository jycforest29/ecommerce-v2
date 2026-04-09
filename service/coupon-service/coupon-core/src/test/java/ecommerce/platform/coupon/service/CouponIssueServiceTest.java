package ecommerce.platform.coupon.service;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.entity.CouponLog;
import ecommerce.platform.coupon.entity.CouponStatus;
import ecommerce.platform.coupon.entity.Promotion;
import ecommerce.platform.coupon.exception.CouponAlreadyIssuedException;
import ecommerce.platform.coupon.exception.CouponSoldOutException;
import ecommerce.platform.coupon.repository.CouponLogRepository;
import ecommerce.platform.coupon.repository.CouponRepository;
import ecommerce.platform.coupon.repository.PromotionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponIssueServiceTest {

    @InjectMocks
    private CouponIssueService couponIssueService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponLogRepository couponLogRepository;

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private RedisTemplate<String, Object> couponRedisRepository;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ListOperations<String, Object> listOperations;

    private Promotion createFixedPromotion() {
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

    private Promotion createRandomPromotion() {
        Promotion promotion = Promotion.builder()
                .promotionName("RANDOM_PROMO")
                .quantity(100)
                .expireDays(30)
                .discountRate(0)
                .randomDiscount(true)
                .minDiscountRate(5)
                .maxDiscountRate(20)
                .minPurchaseAmount(10000)
                .maxDiscountAmount(5000)
                .startedAt(Instant.now())
                .endedAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .category(Category.ALL)
                .brand(Brand.ALL)
                .build();
        ReflectionTestUtils.setField(promotion, "promotionId", 2L);
        return promotion;
    }

    @Nested
    @DisplayName("고정 할인 쿠폰 발급 - issuePromotion")
    class IssueFixedCoupon {

        @Test
        @DisplayName("재고가 있으면 쿠폰이 발급된다")
        void issueSuccess() {
            Promotion promotion = createFixedPromotion();
            given(promotionRepository.findById(1L)).willReturn(Optional.of(promotion));
            given(couponRepository.existsByPromotionAndUserId(promotion, 1L)).willReturn(false);
            given(couponRedisRepository.opsForValue()).willReturn(valueOperations);
            given(valueOperations.decrement("A::OUTER::TEST_PROMO")).willReturn(99L);
            given(couponRepository.save(any(Coupon.class))).willAnswer(invocation -> invocation.getArgument(0));

            var response = couponIssueService.issuePromotion(1L, 1L);

            assertThat(response).isNotNull();
            assertThat(response.discountRate()).isEqualTo(10);
            verify(couponRepository).save(any(Coupon.class));
            verify(couponLogRepository).save(any(CouponLog.class));
        }

        @Test
        @DisplayName("재고가 소진되면 CouponSoldOutException이 발생한다")
        void issueFail_soldOut() {
            Promotion promotion = createFixedPromotion();
            given(promotionRepository.findById(1L)).willReturn(Optional.of(promotion));
            given(couponRepository.existsByPromotionAndUserId(promotion, 1L)).willReturn(false);
            given(couponRedisRepository.opsForValue()).willReturn(valueOperations);
            given(valueOperations.decrement("A::OUTER::TEST_PROMO")).willReturn(-1L);

            assertThatThrownBy(() -> couponIssueService.issuePromotion(1L, 1L))
                    .isInstanceOf(CouponSoldOutException.class);

            verify(valueOperations).increment("A::OUTER::TEST_PROMO");
            verify(couponRepository, never()).save(any());
        }

        @Test
        @DisplayName("이미 발급받은 프로모션이면 CouponAlreadyIssuedException이 발생한다")
        void issueFail_alreadyIssued() {
            Promotion promotion = createFixedPromotion();
            given(promotionRepository.findById(1L)).willReturn(Optional.of(promotion));
            given(couponRepository.existsByPromotionAndUserId(promotion, 1L)).willReturn(true);

            assertThatThrownBy(() -> couponIssueService.issuePromotion(1L, 1L))
                    .isInstanceOf(CouponAlreadyIssuedException.class);

            verify(couponRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 프로모션이면 EntityNotFoundException이 발생한다")
        void issueFail_promotionNotFound() {
            given(promotionRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> couponIssueService.issuePromotion(999L, 1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("랜덤 할인 쿠폰 발급")
    class IssueRandomCoupon {

        @Test
        @DisplayName("리스트에서 LPOP으로 할인율을 꺼내 쿠폰을 발급한다")
        void issueRandomSuccess() {
            Promotion promotion = createRandomPromotion();
            given(promotionRepository.findById(2L)).willReturn(Optional.of(promotion));
            given(couponRepository.existsByPromotionAndUserId(promotion, 1L)).willReturn(false);
            given(couponRedisRepository.opsForList()).willReturn(listOperations);
            given(listOperations.leftPop("ALL::ALL::RANDOM_PROMO::random")).willReturn(15);
            given(couponRepository.save(any(Coupon.class))).willAnswer(invocation -> invocation.getArgument(0));

            var response = couponIssueService.issuePromotion(2L, 1L);

            assertThat(response).isNotNull();
            assertThat(response.discountRate()).isEqualTo(15);
        }

        @Test
        @DisplayName("리스트가 비어있으면 CouponSoldOutException이 발생한다")
        void issueRandomFail_soldOut() {
            Promotion promotion = createRandomPromotion();
            given(promotionRepository.findById(2L)).willReturn(Optional.of(promotion));
            given(couponRepository.existsByPromotionAndUserId(promotion, 1L)).willReturn(false);
            given(couponRedisRepository.opsForList()).willReturn(listOperations);
            given(listOperations.leftPop("ALL::ALL::RANDOM_PROMO::random")).willReturn(null);

            assertThatThrownBy(() -> couponIssueService.issuePromotion(2L, 1L))
                    .isInstanceOf(CouponSoldOutException.class);
        }
    }

    @Nested
    @DisplayName("웰컴 쿠폰 발급 - issueWelcomeCoupon")
    class IssueWelcomeCoupon {

        @Test
        @DisplayName("이미 쿠폰을 보유한 유저에게는 웰컴 쿠폰을 발급하지 않는다")
        void skipIfAlreadyHasCoupon() {
            given(couponRepository.existsByUserId(1L)).willReturn(true);

            couponIssueService.issueWelcomeCoupon(1L);

            verify(promotionRepository, never()).findByPromotionName(any());
        }

        @Test
        @DisplayName("웰컴 프로모션이 없으면 EntityNotFoundException이 발생한다")
        void failIfWelcomePromotionNotFound() {
            given(couponRepository.existsByUserId(1L)).willReturn(false);
            given(promotionRepository.findByPromotionName(Promotion.WELCOME_COUPON)).willReturn(Optional.empty());

            assertThatThrownBy(() -> couponIssueService.issueWelcomeCoupon(1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("유저 쿠폰 전체 비활성화 - deleteAllCouponsByUser")
    class DeleteAllCouponsByUser {

        @Test
        @DisplayName("유저의 모든 쿠폰을 DEACTIVATED 처리하고 로그를 저장한다")
        void deactivateAllCoupons() {
            Promotion promotion = createFixedPromotion();
            Coupon coupon1 = Coupon.of(promotion, 1L, 10);
            Coupon coupon2 = Coupon.of(promotion, 1L, 15);
            given(couponRepository.findAllByUserId(1L)).willReturn(List.of(coupon1, coupon2));

            couponIssueService.deleteAllCouponsByUser(1L);

            assertThat(coupon1.getCouponStatus()).isEqualTo(CouponStatus.DEACTIVATED);
            assertThat(coupon2.getCouponStatus()).isEqualTo(CouponStatus.DEACTIVATED);

            ArgumentCaptor<List<CouponLog>> captor = ArgumentCaptor.forClass(List.class);
            verify(couponLogRepository).saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(2);
        }

        @Test
        @DisplayName("쿠폰이 없는 유저에 대해서도 정상 동작한다")
        void noCoupons() {
            given(couponRepository.findAllByUserId(1L)).willReturn(List.of());

            couponIssueService.deleteAllCouponsByUser(1L);

            verify(couponLogRepository).saveAll(List.of());
        }
    }
}