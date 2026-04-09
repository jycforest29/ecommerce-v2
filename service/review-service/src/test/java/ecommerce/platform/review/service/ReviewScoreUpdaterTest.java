package ecommerce.platform.review.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ReviewScoreUpdaterTest {

    private ReviewScoreUpdater reviewScoreUpdater;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        reviewScoreUpdater = new ReviewScoreUpdater(redisTemplate);
    }

    @Nested
    @DisplayName("점수 추가 - addScore")
    class AddScore {

        @Test
        @DisplayName("점수 합계와 카운트를 증가시킨다")
        void addScoreIncrementsRedis() {
            reviewScoreUpdater.addScore(1L, 5);

            then(valueOperations).should().increment("review:score-sum:1", 5);
            then(valueOperations).should().increment("review:count:1", 1);
        }
    }

    @Nested
    @DisplayName("점수 제거 - removeScore")
    class RemoveScore {

        @Test
        @DisplayName("점수 합계와 카운트를 감소시킨다")
        void removeScoreDecrementsRedis() {
            reviewScoreUpdater.removeScore(1L, 3);

            then(valueOperations).should().increment("review:score-sum:1", -3);
            then(valueOperations).should().increment("review:count:1", -1);
        }
    }

    @Nested
    @DisplayName("평균 점수 조회 - getAverageScore")
    class GetAverageScore {

        @Test
        @DisplayName("합계와 카운트로 평균을 계산한다")
        void getAverageScoreSuccess() {
            given(valueOperations.get("review:score-sum:1")).willReturn("20");
            given(valueOperations.get("review:count:1")).willReturn("5");

            int average = reviewScoreUpdater.getAverageScore(1L);

            assertThat(average).isEqualTo(4);
        }

        @Test
        @DisplayName("합계가 null이면 0을 반환한다")
        void getAverageScoreNullSum() {
            given(valueOperations.get("review:score-sum:1")).willReturn(null);

            int average = reviewScoreUpdater.getAverageScore(1L);

            assertThat(average).isZero();
        }

        @Test
        @DisplayName("카운트가 0이면 0을 반환한다")
        void getAverageScoreZeroCount() {
            given(valueOperations.get("review:score-sum:1")).willReturn("0");
            given(valueOperations.get("review:count:1")).willReturn("0");

            int average = reviewScoreUpdater.getAverageScore(1L);

            assertThat(average).isZero();
        }
    }
}