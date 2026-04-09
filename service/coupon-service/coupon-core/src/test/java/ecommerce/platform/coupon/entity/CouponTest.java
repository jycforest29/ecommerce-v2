package ecommerce.platform.coupon.entity;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    private Promotion createPromotion(Brand brand, Category category, int minPurchaseAmount, int maxDiscountAmount, int expireDays) {
        return Promotion.builder()
                .promotionName("TEST_PROMOTION")
                .quantity(100)
                .expireDays(expireDays)
                .discountRate(10)
                .randomDiscount(false)
                .minDiscountRate(0)
                .maxDiscountRate(0)
                .minPurchaseAmount(minPurchaseAmount)
                .maxDiscountAmount(maxDiscountAmount)
                .startedAt(Instant.now())
                .endedAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .category(category)
                .brand(brand)
                .build();
    }

    private Coupon createCoupon(Promotion promotion, Long userId, int discountRate) {
        return Coupon.of(promotion, userId, discountRate);
    }

    private Coupon createExpiredCoupon(Promotion promotion, Long userId, int discountRate) {
        Coupon coupon = Coupon.of(promotion, userId, discountRate);
        ReflectionTestUtils.setField(coupon, "expiredAt", Instant.now().minus(1, ChronoUnit.DAYS));
        return coupon;
    }

    @Nested
    @DisplayName("쿠폰 생성")
    class Create {

        @Test
        @DisplayName("프로모션 기반으로 쿠폰이 생성된다")
        void createCouponFromPromotion() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);

            Coupon coupon = Coupon.of(promotion, 1L, 10);

            assertThat(coupon.getUserId()).isEqualTo(1L);
            assertThat(coupon.getDiscountRate()).isEqualTo(10);
            assertThat(coupon.getPromotion()).isEqualTo(promotion);
            assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.ISSUED);
            assertThat(coupon.getCreatedAt()).isNotNull();
            assertThat(coupon.getExpiredAt()).isAfter(coupon.getCreatedAt());
        }

        @Test
        @DisplayName("만료일은 프로모션의 expireDays 기준으로 설정된다")
        void expirationBasedOnPromotionExpireDays() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 7);

            Coupon coupon = Coupon.of(promotion, 1L, 10);

            Instant expectedExpiry = coupon.getCreatedAt().plus(7, ChronoUnit.DAYS);
            assertThat(coupon.getExpiredAt()).isEqualTo(expectedExpiry);
        }
    }

    @Nested
    @DisplayName("쿠폰 적용 가능 여부 - isAbleToApply")
    class IsAbleToApply {

        @Test
        @DisplayName("ISSUED 상태이고, 미만료이고, 최소 구매금액 이상이면 적용 가능")
        void ableToApply() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);

            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(15000).quantity(1).build()
            );

            assertThat(coupon.isAbleToApply(items)).isTrue();
        }

        @Test
        @DisplayName("최소 구매금액 미달이면 적용 불가")
        void notAbleToApplyBelowMinPurchase() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);

            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(5000).quantity(1).build()
            );

            assertThat(coupon.isAbleToApply(items)).isFalse();
        }

        @Test
        @DisplayName("브랜드가 다르면 해당 아이템 금액이 집계되지 않아 적용 불가")
        void notAbleToApplyDifferentBrand() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);

            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.B).category(Category.OUTER).price(15000).quantity(1).build()
            );

            assertThat(coupon.isAbleToApply(items)).isFalse();
        }

        @Test
        @DisplayName("카테고리가 다르면 해당 아이템 금액이 집계되지 않아 적용 불가")
        void notAbleToApplyDifferentCategory() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);

            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.SHOES).price(15000).quantity(1).build()
            );

            assertThat(coupon.isAbleToApply(items)).isFalse();
        }

        @Test
        @DisplayName("만료된 쿠폰은 적용 불가")
        void notAbleToApplyExpired() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createExpiredCoupon(promotion, 1L, 10);

            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(15000).quantity(1).build()
            );

            assertThat(coupon.isAbleToApply(items)).isFalse();
        }

        @Test
        @DisplayName("ISSUED 상태가 아니면 적용 불가")
        void notAbleToApplyWhenNotIssued() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);
            coupon.apply();

            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(15000).quantity(1).build()
            );

            assertThat(coupon.isAbleToApply(items)).isFalse();
        }

        @Test
        @DisplayName("여러 아이템의 금액을 합산하여 최소 구매금액을 충족하면 적용 가능")
        void ableToApplyWithMultipleItems() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);

            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(3000).quantity(2).build(),
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(5000).quantity(1).build()
            );

            assertThat(coupon.isAbleToApply(items)).isTrue();
        }
    }

    @Nested
    @DisplayName("할인 금액 계산 - calculateDiscountAmount")
    class CalculateDiscountAmount {

        @Test
        @DisplayName("타겟 금액의 할인율만큼 할인된다")
        void calculateBasicDiscount() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 50000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);

            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(20000).quantity(1).build()
            );

            assertThat(coupon.calculateDiscountAmount(items)).isEqualTo(2000);
        }

        @Test
        @DisplayName("할인 금액이 최대 할인 금액을 초과하면 최대 할인 금액으로 제한된다")
        void discountCappedAtMaxDiscountAmount() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 1000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 50);

            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(20000).quantity(1).build()
            );

            // 50% of 20000 = 10000, but maxDiscountAmount = 1000
            assertThat(coupon.calculateDiscountAmount(items)).isEqualTo(1000);
        }

        @Test
        @DisplayName("브랜드/카테고리가 일치하지 않는 아이템은 할인 대상에서 제외된다")
        void nonMatchingItemsExcluded() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 0, 50000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);

            List<CouponTargetItem> items = List.of(
                    CouponTargetItem.builder().brand(Brand.A).category(Category.OUTER).price(20000).quantity(1).build(),
                    CouponTargetItem.builder().brand(Brand.B).category(Category.SHOES).price(30000).quantity(1).build()
            );

            // Only 20000 is target
            assertThat(coupon.calculateDiscountAmount(items)).isEqualTo(2000);
        }
    }

    @Nested
    @DisplayName("상태 전이")
    class StatusTransition {

        @Test
        @DisplayName("ISSUED → APPLIED 전이 성공")
        void applyFromIssued() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);

            coupon.apply();

            assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.APPLIED);
        }

        @Test
        @DisplayName("APPLIED → APPLY_CANCELLED 전이 성공")
        void rollbackFromApplied() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);
            coupon.apply();

            coupon.rollbackApply();

            assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.APPLY_CANCELLED);
        }

        @Test
        @DisplayName("APPLIED 상태에서 apply()를 호출하면 예외 발생")
        void cannotApplyFromApplied() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);
            coupon.apply();

            assertThatThrownBy(coupon::apply)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("ISSUED 상태에서 rollbackApply()를 호출하면 예외 발생")
        void cannotRollbackFromIssued() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);

            assertThatThrownBy(coupon::rollbackApply)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("expire()는 EXPIRED 상태로 전이한다")
        void expire() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);

            coupon.expire();

            assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.EXPIRED);
        }

        @Test
        @DisplayName("deactivate()는 DEACTIVATED 상태로 전이한다")
        void deactivate() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);

            coupon.deactivate();

            assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.DEACTIVATED);
        }
    }

    @Nested
    @DisplayName("롤백 적용 가능 여부 - isAbleToRollbackApply")
    class IsAbleToRollbackApply {

        @Test
        @DisplayName("APPLIED 상태이고 미만료이면 롤백 가능")
        void ableToRollbackApply() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);
            coupon.apply();

            assertThat(coupon.isAbleToRollbackApply()).isTrue();
        }

        @Test
        @DisplayName("ISSUED 상태이면 롤백 불가")
        void cannotRollbackFromIssued() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);

            assertThat(coupon.isAbleToRollbackApply()).isFalse();
        }

        @Test
        @DisplayName("APPLIED 상태이지만 만료되었으면 롤백 불가")
        void cannotRollbackExpired() {
            Promotion promotion = createPromotion(Brand.A, Category.OUTER, 10000, 5000, 30);
            Coupon coupon = createCoupon(promotion, 1L, 10);
            coupon.apply();
            ReflectionTestUtils.setField(coupon, "expiredAt", Instant.now().minus(1, ChronoUnit.DAYS));

            assertThat(coupon.isAbleToRollbackApply()).isFalse();
        }
    }
}