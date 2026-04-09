package ecommerce.platform.ranking.service;

import ecommerce.platform.common.constants.Category;
import ecommerce.platform.ranking.dto.Period;
import ecommerce.platform.ranking.repository.RankingEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RankingManageServiceTest {

    private RankingManageService rankingManageService;

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @Mock
    private RankingEntryRepository rankingEntryRepository;

    @Mock
    private ZSetOperations<String, Long> zSetOperations;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
        rankingManageService = new RankingManageService(redisTemplate, rankingEntryRepository);
        rankingManageService.init();
    }

    @Nested
    @DisplayName("랭킹 업데이트 - updateRanking")
    class UpdateRanking {

        @Test
        @DisplayName("주문 생성 시 모든 기간의 점수를 증가시킨다")
        void increaseScoreForAllPeriods() {
            rankingManageService.updateRanking(Category.OUTER, 1L, "상품A", 100L, true);

            then(zSetOperations).should().incrementScore("chart::OUTER::REALTIME", 1L, 10);
            then(zSetOperations).should().incrementScore("chart::OUTER::DAILY", 1L, 20);
            then(zSetOperations).should().incrementScore("chart::OUTER::WEEKLY", 1L, 30);
            then(zSetOperations).should().incrementScore("chart::OUTER::MONTHLY", 1L, 40);
        }

        @Test
        @DisplayName("주문 취소 시 모든 기간의 점수를 감소시킨다")
        void decreaseScoreForAllPeriods() {
            rankingManageService.updateRanking(Category.SHOES, 2L, "상품B", 200L, false);

            then(zSetOperations).should().incrementScore("chart::SHOES::REALTIME", 2L, -10);
            then(zSetOperations).should().incrementScore("chart::SHOES::DAILY", 2L, -20);
            then(zSetOperations).should().incrementScore("chart::SHOES::WEEKLY", 2L, -30);
            then(zSetOperations).should().incrementScore("chart::SHOES::MONTHLY", 2L, -40);
        }

        @Test
        @DisplayName("상품 메타데이터를 저장한다")
        void savesRankingEntry() {
            rankingManageService.updateRanking(Category.OUTER, 1L, "상품A", 100L, true);

            then(rankingEntryRepository).should().save(1L, "상품A", 100L);
        }
    }
}
