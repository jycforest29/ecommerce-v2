package ecommerce.platform.ranking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RankingQueryResponseTest {

    @Test
    @DisplayName("RankingEntry로부터 RankingQueryResponse를 생성한다")
    void ofFromRankingEntry() {
        RankingEntry entry = new RankingEntry(1L, "상품A", 100L);

        RankingQueryResponse response = RankingQueryResponse.of(1, entry);

        assertThat(response.rank()).isEqualTo(1);
        assertThat(response.productId()).isEqualTo(1L);
        assertThat(response.productName()).isEqualTo("상품A");
        assertThat(response.imageId()).isEqualTo(100L);
    }
}
