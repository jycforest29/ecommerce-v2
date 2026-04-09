package ecommerce.platform.ranking.repository;

import ecommerce.platform.ranking.dto.RankingEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RankingEntryRepositoryTest {

    private RankingEntryRepository rankingEntryRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Long, RankingEntry> hashOperations;

    private static final String KEY = "ranking:entries";

    @BeforeEach
    void setUp() {
        given(redisTemplate.<Long, RankingEntry>opsForHash()).willReturn(hashOperations);
        rankingEntryRepository = new RankingEntryRepository(redisTemplate);
        rankingEntryRepository.init();
    }

    @Test
    @DisplayName("productId로 RankingEntry를 조회한다")
    void get() {
        RankingEntry entry = new RankingEntry(1L, "상품A", 100L);
        given(hashOperations.get(KEY, 1L)).willReturn(entry);

        RankingEntry result = rankingEntryRepository.get(1L);

        assertThat(result.productId()).isEqualTo(1L);
        assertThat(result.productName()).isEqualTo("상품A");
        assertThat(result.imageId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("존재하지 않는 productId 조회 시 null을 반환한다")
    void getNotFound() {
        given(hashOperations.get(KEY, 999L)).willReturn(null);

        RankingEntry result = rankingEntryRepository.get(999L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("새로운 상품을 저장한다")
    void saveNew() {
        given(hashOperations.get(KEY, 1L)).willReturn(null);

        rankingEntryRepository.save(1L, "상품A", 100L);

        then(hashOperations).should().put(KEY, 1L, new RankingEntry(1L, "상품A", 100L));
    }

    @Test
    @DisplayName("이미 존재하는 상품은 저장하지 않는다")
    void saveExisting() {
        RankingEntry existing = new RankingEntry(1L, "상품A", 100L);
        given(hashOperations.get(KEY, 1L)).willReturn(existing);

        rankingEntryRepository.save(1L, "상품A 수정", 200L);

        then(hashOperations).should(never()).put(KEY, 1L, new RankingEntry(1L, "상품A 수정", 200L));
    }
}
