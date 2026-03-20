package ecommerce.platform.ranking.service;

import ecommerce.platform.common.constants.Category;
import ecommerce.platform.ranking.dto.Period;
import ecommerce.platform.ranking.repository.RankingEntryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingManageService {
    private static final String KEY = "chart::%s::%s";
    private final RedisTemplate<String, Long> redisTemplate;
    private final RankingEntryRepository rankingEntryRepository;
    private ZSetOperations<String, Long> zSetOperations;

    @PostConstruct
    public void init() {
        zSetOperations = redisTemplate.opsForZSet();
    }

    public void updateRanking(Category category,
                              Long productId,
                              String productName,
                              Long imageId, boolean isIncrease) {
        for (Period period : Period.values()) {
            String key = KEY.formatted(category, period);
            zSetOperations.incrementScore(key, productId, isIncrease ? period.getIncrement() : -period.getIncrement());
        }
        rankingEntryRepository.save(productId, productName, imageId);
    }
}