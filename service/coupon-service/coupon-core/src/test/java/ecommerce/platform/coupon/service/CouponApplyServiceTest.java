package ecommerce.platform.coupon.service;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.common.event.coupon.CouponApplyRequestEvent;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.coupon.dto.CouponApplyRequest;
import ecommerce.platform.coupon.dto.CouponApplyResponse;
import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.entity.CouponLog;
import ecommerce.platform.coupon.entity.CouponStatus;
import ecommerce.platform.coupon.entity.CouponTargetItem;
import ecommerce.platform.coupon.entity.Promotion;
import ecommerce.platform.coupon.exception.CouponFailedToApplyException;
import ecommerce.platform.coupon.exception.CouponFailedToRollbackApplyException;
import ecommerce.platform.coupon.repository.CouponLogRepository;
import ecommerce.platform.coupon.repository.CouponRepository;
import ecommerce.platform.coupon.repository.OutboxEventRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponApplyServiceTest {

    @InjectMocks
    private CouponApplyService couponApplyService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponLogRepository couponLogRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

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

    private Coupon createCoupon(Long couponId, Long userId) {
        Promotion promotion = createPromotion();
        Coupon coupon = Coupon.of(promotion, userId, 10);
        ReflectionTestUtils.setField(coupon, "couponId", couponId);
        return coupon;
    }

    @Nested
    @DisplayName("수동 쿠폰 적용 - apply")
    class Apply {

        @Test
        @DisplayName("유효한 쿠폰 적용 시 할인 응답을 반환한다")
        void applySuccess() {
            Coupon coupon = createCoupon(1L, 1L);
            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(20000).quantity(1).build()
            );
            CouponApplyRequest request = new CouponApplyRequest(items);

            given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));
            given(couponRepository.applyConditionally(1L)).willReturn(1);

            CouponApplyResponse response = couponApplyService.apply(1L, 1L, request);

            assertThat(response.originalPrice()).isEqualTo(20000);
            assertThat(response.discountAmount()).isEqualTo(2000);
            assertThat(response.finalPrice()).isEqualTo(18000);
            verify(couponLogRepository).save(any(CouponLog.class));
        }

        @Test
        @DisplayName("다른 유저의 쿠폰을 적용하면 CouponFailedToApplyException 발생")
        void applyFail_differentUser() {
            Coupon coupon = createCoupon(1L, 2L);  // owner is userId=2
            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(20000).quantity(1).build()
            );
            CouponApplyRequest request = new CouponApplyRequest(items);

            given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));

            assertThatThrownBy(() -> couponApplyService.apply(1L, 1L, request))
                    .isInstanceOf(CouponFailedToApplyException.class);
        }

        @Test
        @DisplayName("적용 조건을 만족하지 않으면 CouponFailedToApplyException 발생")
        void applyFail_notEligible() {
            Coupon coupon = createCoupon(1L, 1L);
            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(5000).quantity(1).build()
            );
            CouponApplyRequest request = new CouponApplyRequest(items);

            given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));

            assertThatThrownBy(() -> couponApplyService.apply(1L, 1L, request))
                    .isInstanceOf(CouponFailedToApplyException.class);
        }

        @Test
        @DisplayName("applyConditionally가 0을 반환하면 CouponFailedToApplyException 발생")
        void applyFail_conditionalUpdateFailed() {
            Coupon coupon = createCoupon(1L, 1L);
            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(20000).quantity(1).build()
            );
            CouponApplyRequest request = new CouponApplyRequest(items);

            given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));
            given(couponRepository.applyConditionally(1L)).willReturn(0);

            assertThatThrownBy(() -> couponApplyService.apply(1L, 1L, request))
                    .isInstanceOf(CouponFailedToApplyException.class);
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰이면 EntityNotFoundException 발생")
        void applyFail_couponNotFound() {
            given(couponRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> couponApplyService.apply(999L, 1L, new CouponApplyRequest(List.of())))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("수동 쿠폰 롤백 - rollbackApply")
    class RollbackApply {

        @Test
        @DisplayName("APPLIED 상태의 본인 쿠폰 롤백 성공")
        void rollbackSuccess() {
            Coupon coupon = createCoupon(1L, 1L);
            coupon.apply();  // APPLIED 상태로 전이
            given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));
            given(couponRepository.rollbackApplyConditionally(1L)).willReturn(1);

            couponApplyService.rollbackApply(1L, 1L);

            verify(couponLogRepository).save(any(CouponLog.class));
        }

        @Test
        @DisplayName("다른 유저의 쿠폰 롤백 시 CouponFailedToRollbackApplyException 발생")
        void rollbackFail_differentUser() {
            Coupon coupon = createCoupon(1L, 2L);
            coupon.apply();
            given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));

            assertThatThrownBy(() -> couponApplyService.rollbackApply(1L, 1L))
                    .isInstanceOf(CouponFailedToRollbackApplyException.class);
        }

        @Test
        @DisplayName("ISSUED 상태 쿠폰 롤백 시 CouponFailedToRollbackApplyException 발생")
        void rollbackFail_notApplied() {
            Coupon coupon = createCoupon(1L, 1L);  // ISSUED 상태
            given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));

            assertThatThrownBy(() -> couponApplyService.rollbackApply(1L, 1L))
                    .isInstanceOf(CouponFailedToRollbackApplyException.class);
        }

        @Test
        @DisplayName("rollbackApplyConditionally가 0을 반환하면 CouponFailedToRollbackApplyException 발생")
        void rollbackFail_conditionalUpdateFailed() {
            Coupon coupon = createCoupon(1L, 1L);
            coupon.apply();
            given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));
            given(couponRepository.rollbackApplyConditionally(1L)).willReturn(0);

            assertThatThrownBy(() -> couponApplyService.rollbackApply(1L, 1L))
                    .isInstanceOf(CouponFailedToRollbackApplyException.class);
        }
    }

    @Nested
    @DisplayName("주문 이벤트 기반 쿠폰 적용 - applyFromOrder")
    class ApplyFromOrder {

        @Test
        @DisplayName("적용 가능한 쿠폰이 있으면 적용하고 OutboxEvent를 저장한다")
        void applyFromOrderSuccess() {
            // applyFromOrder는 CouponTargetItem에 brand/category를 세팅하지 않으므로
            // ALL/ALL 프로모션 쿠폰이어야 isAbleToApply에서 매칭된다
            Promotion allPromotion = Promotion.builder()
                    .promotionName("ALL_PROMO")
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
                    .category(Category.ALL)
                    .brand(Brand.ALL)
                    .build();
            ReflectionTestUtils.setField(allPromotion, "promotionId", 1L);
            Coupon coupon = Coupon.of(allPromotion, 1L, 10);
            ReflectionTestUtils.setField(coupon, "couponId", 1L);

            CouponApplyRequestEvent event = CouponApplyRequestEvent.builder()
                    .orderId(100L)
                    .userId(1L)
                    .orderItemInfos(List.of(
                            new CouponApplyRequestEvent.OrderItemInfo(1L, 1L, 20000, 1)
                    ))
                    .build();

            given(couponRepository.findAllByUserId(1L)).willReturn(List.of(coupon));
            given(couponRepository.applyConditionally(1L)).willReturn(1);

            couponApplyService.applyFromOrder(event);

            verify(outboxEventRepository).save(any(OutboxEvent.class));
            verify(couponLogRepository).save(any(CouponLog.class));
        }

        @Test
        @DisplayName("적용 가능한 쿠폰이 없으면 OutboxEvent를 저장하지 않는다")
        void applyFromOrderNoCouponAvailable() {
            CouponApplyRequestEvent event = CouponApplyRequestEvent.builder()
                    .orderId(100L)
                    .userId(1L)
                    .orderItemInfos(List.of(
                            new CouponApplyRequestEvent.OrderItemInfo(1L, 1L, 5000, 1)
                    ))
                    .build();

            given(couponRepository.findAllByUserId(1L)).willReturn(List.of());

            couponApplyService.applyFromOrder(event);

            verify(outboxEventRepository, never()).save(any());
        }

        @Test
        @DisplayName("여러 쿠폰 중 첫 번째 적용 가능한 쿠폰만 적용된다")
        void applyFromOrderFirstEligible() {
            Coupon ineligibleCoupon = createCoupon(1L, 1L);
            // minPurchaseAmount가 10000이므로 5000원짜리로는 적용 불가

            Promotion allPromotion = Promotion.builder()
                    .promotionName("ALL_PROMO")
                    .quantity(100)
                    .expireDays(30)
                    .discountRate(5)
                    .randomDiscount(false)
                    .minDiscountRate(0)
                    .maxDiscountRate(0)
                    .minPurchaseAmount(1000)
                    .maxDiscountAmount(5000)
                    .startedAt(Instant.now())
                    .endedAt(Instant.now().plus(30, ChronoUnit.DAYS))
                    .category(Category.ALL)
                    .brand(Brand.ALL)
                    .build();
            ReflectionTestUtils.setField(allPromotion, "promotionId", 2L);

            Coupon eligibleCoupon = Coupon.of(allPromotion, 1L, 5);
            ReflectionTestUtils.setField(eligibleCoupon, "couponId", 2L);

            CouponApplyRequestEvent event = CouponApplyRequestEvent.builder()
                    .orderId(100L)
                    .userId(1L)
                    .orderItemInfos(List.of(
                            new CouponApplyRequestEvent.OrderItemInfo(1L, 1L, 5000, 1)
                    ))
                    .build();

            given(couponRepository.findAllByUserId(1L)).willReturn(List.of(ineligibleCoupon, eligibleCoupon));
            given(couponRepository.applyConditionally(2L)).willReturn(1);

            couponApplyService.applyFromOrder(event);

            verify(couponRepository).applyConditionally(2L);
            verify(couponRepository, never()).applyConditionally(1L);
            verify(outboxEventRepository).save(any(OutboxEvent.class));
        }
    }
}
