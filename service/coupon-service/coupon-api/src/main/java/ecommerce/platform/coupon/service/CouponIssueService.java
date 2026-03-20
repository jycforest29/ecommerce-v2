package ecommerce.platform.coupon.service;

import ecommerce.platform.coupon.exception.CouponAlreadyIssuedException;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.coupon.dto.CouponIssueResponse;
import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.entity.CouponLog;
import ecommerce.platform.coupon.entity.Promotion;
import ecommerce.platform.coupon.exception.CouponSoldOutException;
import ecommerce.platform.coupon.repository.CouponLogRepository;
import ecommerce.platform.coupon.repository.CouponRepository;
import ecommerce.platform.coupon.repository.PromotionRepository;
import ecommerce.platform.coupon.util.RedisKeyConverterUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponIssueService {
    private final CouponRepository couponRepository;
    private final CouponLogRepository couponLogRepository;
    private final PromotionRepository promotionRepository;
    private final RedisTemplate<String, Object> couponRedisRepository;

    @Transactional
    public void issueWelcomeCoupon(Long userId) {
        if (couponRepository.existsByUserId(userId)) return;
        Promotion promotion = promotionRepository.findByName(Promotion.WELCOME_COUPON)
                .orElseThrow(() -> new EntityNotFoundException());
        issuePromotion(promotion.getPromotionId(), userId);
    }

    @Transactional
    public CouponIssueResponse issuePromotion(Long promotionId, Long userId) {
        final Promotion promotion = EntityFinder.findEntity(promotionRepository, promotionId);

        if (couponRepository.existsByPromotionAndUserId(promotion, userId))
            throw new CouponAlreadyIssuedException(promotionId, userId);

        String key = RedisKeyConverterUtil.toKey(promotion);
        int rate;

        if (promotion.isRandomDiscount()) {
            // 랜덤 쿠폰: RPUSH로 적재된 리스트에서 LPOP으로 꺼냄
            String listKey = key + "::random";
            Object popped = couponRedisRepository.opsForList().leftPop(listKey);
            if (popped == null) {
                throw new CouponSoldOutException();
            }
            rate = (int) popped;
        } else {
            // 고정 할인: DECR로 재고 차감
            Long remaining = couponRedisRepository.opsForValue().decrement(key);
            if (remaining == null || remaining < 0) {
                couponRedisRepository.opsForValue().increment(key);
                throw new CouponSoldOutException();
            }
            rate = promotion.getDiscountRate();
        }
        Coupon coupon = Coupon.of(promotion, userId, rate);
        couponRepository.save(coupon);

        CouponLog couponLog = CouponLogFactory.issue(coupon);
        couponLogRepository.save(couponLog);

        return CouponIssueResponse.from(coupon);
    }

    @Transactional
    public void deleteAllCouponsByUser(Long userId) {
        List<Coupon> coupons = couponRepository.findAllByUserId(userId);
        List<CouponLog> couponLogs = coupons.stream()
                .map(CouponLogFactory::deactivate)
                .toList();
        couponLogRepository.saveAll(couponLogs);
        coupons.forEach(Coupon::deactivate);
    }
}