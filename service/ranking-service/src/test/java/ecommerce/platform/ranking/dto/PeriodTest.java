package ecommerce.platform.ranking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodTest {

    @Test
    @DisplayName("각 Period별 increment 값이 올바르다")
    void incrementValues() {
        assertThat(Period.REALTIME.getIncrement()).isEqualTo(10);
        assertThat(Period.DAILY.getIncrement()).isEqualTo(20);
        assertThat(Period.WEEKLY.getIncrement()).isEqualTo(30);
        assertThat(Period.MONTHLY.getIncrement()).isEqualTo(40);
    }
}
