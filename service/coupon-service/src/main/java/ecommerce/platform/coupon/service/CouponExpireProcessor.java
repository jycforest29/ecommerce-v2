package ecommerce.platform.coupon.service;

import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.entity.CouponLog;
import ecommerce.platform.coupon.repository.CouponLogRepository;
import ecommerce.platform.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponExpireProcessor {

    private final CouponRepository couponRepository;
    private final CouponLogRepository couponLogRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void expireCoupons() {
        List<Coupon> expiredCoupons = couponRepository.findAllCouponsToExpire();

        List<CouponLog> couponLogs = expiredCoupons.stream()
                .map(CouponLogFactory::expire)
                .toList();
        couponLogRepository.saveAll(couponLogs);
        expiredCoupons.forEach(Coupon::expire);
    }
}
