package ecommerce.platform.coupon.entity;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionTest {

    @Test
    @DisplayName("고정 할인 프로모션은 discountRate를 그대로 반환한다")
    void fixedDiscountRateReturnsExactRate() {
        Promotion promotion = Promotion.builder()
                .promotionName("FIXED_PROMO")
                .quantity(100)
                .expireDays(30)
                .discountRate(15)
                .randomDiscount(false)
                .minDiscountRate(0)
                .maxDiscountRate(0)
                .minPurchaseAmount(10000)
                .maxDiscountAmount(5000)
                .startedAt(Instant.now())
                .endedAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .category(Category.ACCESSORY)
                .brand(Brand.A)
                .build();

        assertThat(promotion.resolveDiscountRate()).isEqualTo(15);
    }

    @Test
    @DisplayName("랜덤 할인 프로모션은 min~max 범위 내의 할인율을 반환한다")
    void randomDiscountRateWithinRange() {
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
                .category(Category.OUTER)
                .brand(Brand.A)
                .build();

        for (int i = 0; i < 100; i++) {
            int rate = promotion.resolveDiscountRate();
            assertThat(rate).isBetween(5, 20);
        }
    }

    @Test
    @DisplayName("WELCOME_COUPON 상수는 ALL::ALL::WELCOME이다")
    void welcomeCouponConstant() {
        assertThat(Promotion.WELCOME_COUPON).isEqualTo("ALL::ALL::WELCOME");
    }
}