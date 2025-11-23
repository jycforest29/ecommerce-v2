package ecommerce.platform.coupon.repository;

import ecommerce.platform.coupon.entity.CouponLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponLogRepository extends JpaRepository<CouponLog, Long> { }
