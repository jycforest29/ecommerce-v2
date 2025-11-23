package ecommerce.platform.ranking.repository;

import ecommerce.platform.ranking.dto.RankingEntry;
import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RankingEntryRepository {
    private final RedisTemplate<Long, RankingEntry> redisTemplate;
    private HashOperations<Long, Long, RankingEntry> hashOperations;

    @PostConstruct
    public void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    public RankingEntry get(Long productId) {
        return hashOperations.get(productId, productId);
    }

    @Transactional
    public void save(Long productId, String productName, Long imageId) {
        if (get(productId) != null) return;
        productRedisTemplate.opsForSet()
                .add(productId, RankingEntry.create(
                        productId, productName, imageId
                ));
    }
}
