package ecommerce.platform.ranking.service;

import ecommerce.platform.common.constants.Category;
import ecommerce.platform.ranking.dto.Period;
import ecommerce.platform.ranking.dto.RankingDto;
import ecommerce.platform.ranking.dto.RankingEntry;
import ecommerce.platform.ranking.repository.RankingEntryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RankingQueryService {
    private static final String KEY = "chart::{0}::{1}";
    private final RedisTemplate<String, Long> redisTemplate;
    private final RankingEntryRepository rankingEntryRepository;
    private ZSetOperations<String, Long> zSetOperations;

    @PostConstruct
    public void init() {
        zSetOperations = redisTemplate.opsForZSet();
    }

    public Set<RankingDto> getRanking(Category category, Period period) {
        String key = getKey(category, period);
        return getRankingInternal(key);
    }

    private Set<RankingDto> getRankingInternal(String key) {
        final AtomicInteger rank = new AtomicInteger(1);
        return redisTemplate.opsForZSet()
                .reverseRange(key, 0, 10)
                .stream()
                .map(productId -> {
                    RankingEntry rankingEntry = rankingEntryRepository.get(productId);
                    return RankingDto.of(rank.getAndIncrement(), rankingEntry);
                })
                .collect(Collectors.toSet());
    }

    public void updateRanking(Category category,
                              Long productId,
                              String productName,
                              Long imageId, boolean isIncrease) {
        String key;
        for (Period period : Period.values()) {
            key = getKey(category, period);
            zSetOperations.add(key, productId, isIncrease ? period.getIncrement() : -period.getIncrement());
        }
        rankingEntryRepository.save(productId, productName, imageId);
    }

    private String getKey(Category category, Period period) {
        return KEY.formatted(category, period);
    }
}
