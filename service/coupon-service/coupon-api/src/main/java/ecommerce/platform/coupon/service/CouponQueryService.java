package ecommerce.platform.coupon.service;

import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.coupon.dto.CouponQueryResponse;
import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class CouponQueryService {
    private final CouponRepository couponRepository;

    public Page<CouponQueryResponse> getAllIssuedCoupons(Long userId, Pageable pageable) {
        return couponRepository.findAllByUserId(userId, pageable)
                .map(CouponQueryResponse::from);
    }

    public CouponQueryResponse getIssuedCoupon(Long userId, Long couponId) {
        Coupon coupon = EntityFinder.findEntity(couponRepository, couponId);
        if (!coupon.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException();
        }
        return CouponQueryResponse.from(coupon);
    }
}