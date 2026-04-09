package ecommerce.platform.review.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewScoreTest {

    @Test
    @DisplayName("rateScore로 ReviewScore를 조회한다")
    void ofValidScore() {
        assertThat(ReviewScore.of(1)).isEqualTo(ReviewScore.ONE);
        assertThat(ReviewScore.of(2)).isEqualTo(ReviewScore.TWO);
        assertThat(ReviewScore.of(3)).isEqualTo(ReviewScore.THREE);
        assertThat(ReviewScore.of(4)).isEqualTo(ReviewScore.FOUR);
        assertThat(ReviewScore.of(5)).isEqualTo(ReviewScore.FIVE);
    }

    @Test
    @DisplayName("유효하지 않은 rateScore이면 예외가 발생한다")
    void ofInvalidScore() {
        assertThatThrownBy(() -> ReviewScore.of(0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> ReviewScore.of(6))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("각 ReviewScore의 comment와 rateScore가 올바르다")
    void commentAndRateScore() {
        assertThat(ReviewScore.ONE.getComment()).isEqualTo("별로예요");
        assertThat(ReviewScore.ONE.getRateScore()).isEqualTo(1);
        assertThat(ReviewScore.FIVE.getComment()).isEqualTo("아주 좋아요");
        assertThat(ReviewScore.FIVE.getRateScore()).isEqualTo(5);
    }
}