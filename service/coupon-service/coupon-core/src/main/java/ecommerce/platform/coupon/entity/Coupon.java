package ecommerce.platform.coupon.entity;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long couponId;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Promotion promotion;

    @Column(nullable = false)
    private int discountRate;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiredAt;

    @Enumerated(value = EnumType.STRING)
    private CouponStatus couponStatus = CouponStatus.ISSUED;

    private Coupon(Long userId, int discountRate, Promotion promotion) {
        this.userId = userId;
        this.discountRate = discountRate;
        this.promotion = promotion;
        this.createdAt = Instant.now();
        this.expiredAt = createdAt.plus(Duration.ofDays(promotion.getExpireDays()));
    }

    public static Coupon of(Promotion promotion, Long userId, int discountRate) {
        return new Coupon(userId, discountRate, promotion);
    }

    public boolean isAbleToApply(List<CouponTargetItem> orderItems) {
        if (couponStatus != CouponStatus.ISSUED || isExpired()) return false;

        final Brand brand = promotion.getBrand();
        final Category category = promotion.getCategory();
        final int minPurchaseAmount = promotion.getMinPurchaseAmount();

        int totalPrice = orderItems.stream()
                .filter(orderItem -> brand.equalsTo(orderItem.getBrand()) && category.equalsTo(orderItem.getCategory()))
                .mapToInt(orderItem -> orderItem.getPrice() * orderItem.getQuantity())
                .sum();

        return totalPrice >= minPurchaseAmount;
    }

    public int calculateDiscountAmount(List<CouponTargetItem> orderItems) {
        final Brand brand = promotion.getBrand();
        final Category category = promotion.getCategory();
        final int maxDiscountAmount = promotion.getMaxDiscountAmount();

        int targetPrice = orderItems.stream()
                .filter(orderItem -> brand.equalsTo(orderItem.getBrand()) && category.equalsTo(orderItem.getCategory()))
                .mapToInt(orderItem -> orderItem.getPrice() * orderItem.getQuantity())
                .sum();

        int discounted = targetPrice * discountRate / 100;
        return Math.min(discounted, maxDiscountAmount);
    }

    public boolean isAbleToRollbackApply() {
        return couponStatus == CouponStatus.APPLIED && !isExpired();
    }

    private boolean isExpired() {
        return Instant.now().isAfter(expiredAt);
    }

    public void apply() {
        validateStatusTransition(CouponStatus.ISSUED, CouponStatus.APPLIED);
        this.couponStatus = CouponStatus.APPLIED;
    }

    public void rollbackApply() {
        validateStatusTransition(CouponStatus.APPLIED, CouponStatus.APPLY_CANCELLED);
        this.couponStatus = CouponStatus.APPLY_CANCELLED;
    }

    public void expire() {
        this.couponStatus = CouponStatus.EXPIRED;
    }

    public void deactivate() {
        this.couponStatus = CouponStatus.DEACTIVATED;
    }

    private void validateStatusTransition(CouponStatus expected, CouponStatus target) {
        if (this.couponStatus != expected) {
            throw new IllegalStateException(
                    expected + " 상태에서만 " + target + "으로 전이 가능합니다. 현재: " + this.couponStatus);
        }
    }
}
