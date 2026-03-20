package ecommerce.platform.coupon.service;

import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.coupon.dto.PromotionRegisterRequest;
import ecommerce.platform.coupon.dto.PromotionRegisterResponse;
import ecommerce.platform.coupon.entity.Promotion;
import ecommerce.platform.coupon.repository.PromotionRepository;
import ecommerce.platform.coupon.util.RandomUtil;
import ecommerce.platform.coupon.util.RedisKeyConverterUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromotionManageService {
    private final PromotionRepository promotionRepository;
    private final RedisTemplate<String, Object> couponRedisRepository;

    @Transactional
    public PromotionRegisterResponse register(PromotionRegisterRequest promotionRegisterRequest) {
        Promotion promotion = promotionRegisterRequest.toEntity();
        promotionRepository.save(promotion);
        String key = RedisKeyConverterUtil.toKey(promotion);

        if (promotion.isRandomDiscount()) {
            String listKey = key + "::random";
            for (int i = 0; i < promotion.getQuantity(); i++) {
                int rate = RandomUtil.random(promotion.getMinDiscountRate(), promotion.getMaxDiscountRate() + 1);
                couponRedisRepository.opsForList().rightPush(listKey, rate);
            }
        } else {
            couponRedisRepository.opsForValue().set(key, promotion.getQuantity());
        }

        return PromotionRegisterResponse.from(promotion);
    }

    @Transactional
    public void remove(Long promotionId) {
        Promotion promotion = EntityFinder.findEntity(promotionRepository, promotionId);
        String key = RedisKeyConverterUtil.toKey(promotion);
        couponRedisRepository.delete(key);
        couponRedisRepository.delete(key + "::random");
        promotionRepository.delete(promotion);
    }
}
