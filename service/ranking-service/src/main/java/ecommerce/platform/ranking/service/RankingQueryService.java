package ecommerce.platform.ranking.service;

import ecommerce.platform.common.constants.Category;
import ecommerce.platform.ranking.dto.Period;
import ecommerce.platform.ranking.dto.RankingQueryResponse;
import ecommerce.platform.ranking.dto.RankingEntry;
import ecommerce.platform.ranking.repository.RankingEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingQueryService {
    private static final String KEY = "chart::%s::%s";
    private final RedisTemplate<String, Long> redisTemplate;
    private final RankingEntryRepository rankingEntryRepository;

    public List<RankingQueryResponse> getRanking(Category category, Period period) {
        String key = KEY.formatted(category, period);
        return getRankingInternal(key);
    }

    private List<RankingQueryResponse> getRankingInternal(String key) {
        final AtomicInteger rank = new AtomicInteger(1);
        return redisTemplate.opsForZSet()
                .reverseRange(key, 0, 9)
                .stream()
                .map(productId -> {
                    RankingEntry rankingEntry = rankingEntryRepository.get(productId);
                    return RankingQueryResponse.of(rank.getAndIncrement(), rankingEntry);
                })
                .collect(Collectors.toList());
    }
}