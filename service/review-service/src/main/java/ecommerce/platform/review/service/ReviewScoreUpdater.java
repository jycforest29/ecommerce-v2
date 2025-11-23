package ecommerce.platform.review.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class ReviewScoreUpdater {

    private static final String SCORE_SUM_KEY = "review:score-sum:";
    private static final String COUNT_KEY = "review:count:";

    private final ValueOperations<String, String> valueOperations;

    public ReviewScoreUpdater(RedisTemplate<String, String> redisTemplate) {
        this.valueOperations = redisTemplate.opsForValue();
    }

    public void addScore(Long productId, int rateScore) {
        valueOperations.increment(SCORE_SUM_KEY + productId, rateScore);
        valueOperations.increment(COUNT_KEY + productId, 1);
    }

    public void removeScore(Long productId, int rateScore) {
        valueOperations.increment(SCORE_SUM_KEY + productId, -rateScore);
        valueOperations.increment(COUNT_KEY + productId, -1);
    }

    public int getAverageScore(Long productId) {
        String sum = valueOperations.get(SCORE_SUM_KEY + productId);
        String count = valueOperations.get(COUNT_KEY + productId);
        if (sum == null || count == null || "0".equals(count)) {
            return 0;
        }
        return Integer.parseInt(sum) / Integer.parseInt(count);
    }
}