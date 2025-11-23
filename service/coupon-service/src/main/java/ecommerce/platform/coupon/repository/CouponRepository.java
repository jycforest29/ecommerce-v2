package ecommerce.platform.coupon.repository;

import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findAllByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByPromotionAndUserId(Promotion promotion, Long userId);

    @Query("SELECT c FROM Coupon c WHERE c.expiredAt <= CURRENT_TIMESTAMP AND c.couponStatus = 'ISSUED'")
    List<Coupon> findAllCouponsToExpire();
}
