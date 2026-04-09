package ecommerce.platform.ranking.service;

import ecommerce.platform.common.constants.Category;
import ecommerce.platform.ranking.dto.Period;
import ecommerce.platform.ranking.dto.RankingEntry;
import ecommerce.platform.ranking.dto.RankingQueryResponse;
import ecommerce.platform.ranking.repository.RankingEntryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RankingQueryServiceTest {

    @InjectMocks
    private RankingQueryService rankingQueryService;

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @Mock
    private RankingEntryRepository rankingEntryRepository;

    @Mock
    private ZSetOperations<String, Long> zSetOperations;

    @Nested
    @DisplayName("랭킹 조회 - getRanking")
    class GetRanking {

        @Test
        @DisplayName("카테고리와 기간별 상위 10개 상품 랭킹을 반환한다")
        void getRankingSuccess() {
            String key = "chart::OUTER::DAILY";
            Set<Long> productIds = new LinkedHashSet<>(List.of(3L, 1L, 2L));

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.reverseRange(key, 0, 9)).willReturn(productIds);
            given(rankingEntryRepository.get(3L)).willReturn(new RankingEntry(3L, "상품C", 300L));
            given(rankingEntryRepository.get(1L)).willReturn(new RankingEntry(1L, "상품A", 100L));
            given(rankingEntryRepository.get(2L)).willReturn(new RankingEntry(2L, "상품B", 200L));

            List<RankingQueryResponse> result = rankingQueryService.getRanking(Category.OUTER, Period.DAILY);

            assertThat(result).hasSize(3);
            assertThat(result.get(0).rank()).isEqualTo(1);
            assertThat(result.get(0).productId()).isEqualTo(3L);
            assertThat(result.get(0).productName()).isEqualTo("상품C");
            assertThat(result.get(1).rank()).isEqualTo(2);
            assertThat(result.get(1).productId()).isEqualTo(1L);
            assertThat(result.get(2).rank()).isEqualTo(3);
            assertThat(result.get(2).productId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("랭킹 데이터가 없으면 빈 리스트를 반환한다")
        void getRankingEmpty() {
            String key = "chart::SHOES::REALTIME";

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.reverseRange(key, 0, 9)).willReturn(new LinkedHashSet<>());

            List<RankingQueryResponse> result = rankingQueryService.getRanking(Category.SHOES, Period.REALTIME);

            assertThat(result).isEmpty();
        }
    }
}
