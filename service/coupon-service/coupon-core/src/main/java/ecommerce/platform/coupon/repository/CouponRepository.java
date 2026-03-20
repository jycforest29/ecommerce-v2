package ecommerce.platform.coupon.repository;

import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findAllByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByPromotionAndUserId(Promotion promotion, Long userId);

    @Query("SELECT c FROM Coupon c WHERE c.expiredAt <= CURRENT_TIMESTAMP AND c.couponStatus = 'ISSUED'")
    List<Coupon> findAllCouponsToExpire();

    @Modifying
    @Query("UPDATE Coupon c SET c.couponStatus = ecommerce.platform.coupon.entity.CouponStatus.APPLIED " +
            "WHERE c.couponId = :couponId AND c.couponStatus = ecommerce.platform.coupon.entity.CouponStatus.ISSUED " +
            "AND c.expiredAt > CURRENT_TIMESTAMP")
    int applyConditionally(@Param("couponId") Long couponId);

    @Modifying
    @Query("UPDATE Coupon c SET c.couponStatus = ecommerce.platform.coupon.entity.CouponStatus.APPLY_CANCELLED " +
            "WHERE c.couponId = :couponId AND c.couponStatus = ecommerce.platform.coupon.entity.CouponStatus.APPLIED")
    int rollbackApplyConditionally(@Param("couponId") Long couponId);
}
