package ecommerce.platform.ranking.repository;

import ecommerce.platform.ranking.dto.RankingEntry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RankingEntryRepository {
    private static final String KEY = "ranking:entries";
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, Long, RankingEntry> hashOperations;

    @PostConstruct
    public void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    public RankingEntry get(Long productId) {
        return hashOperations.get(KEY, productId);
    }

    public void save(Long productId, String productName, Long imageId) {
        if (get(productId) != null) return;
        hashOperations.put(KEY, productId, new RankingEntry(productId, productName, imageId));
    }
}
