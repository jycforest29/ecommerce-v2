package ecommerce.platform.coupon.service;

import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.coupon.dto.CouponQueryResponse;
import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponQueryService {
    private final CouponRepository couponRepository;

    public List<CouponQueryResponse> getAllIssuedCoupons(Long userId) {
        return couponRepository.findAllByUserId(userId)
                .stream()
                .map(CouponQueryResponse::from)
                .toList();
    }

    public CouponQueryResponse getIssuedCoupon(Long userId, Long couponId) {
        Coupon coupon = EntityFinder.findEntity(couponRepository, couponId);
        if (!coupon.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException();
        }
        return CouponQueryResponse.from(coupon);
    }
}