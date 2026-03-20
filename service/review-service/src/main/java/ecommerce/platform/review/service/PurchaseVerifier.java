package ecommerce.platform.review.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;

@Component
public class PurchaseVerifier {

    private static final String KEY_PREFIX = "purchased:product:";

    private final SetOperations<String, Object> setOperations;

    public PurchaseVerifier(RedisTemplate<String, Object> redisTemplate) {
        this.setOperations = redisTemplate.opsForSet();
    }

    public void addPurchase(Long productId, Long userId) {
        setOperations.add(toKey(productId), userId);
    }

    public void removePurchase(Long productId, Long userId) {
        setOperations.remove(toKey(productId), userId);
    }

    public boolean hasPurchased(Long productId, Long userId) {
        return Boolean.TRUE.equals(setOperations.isMember(toKey(productId), userId));
    }

    private String toKey(Long productId) {
        return KEY_PREFIX + productId;
    }
}
