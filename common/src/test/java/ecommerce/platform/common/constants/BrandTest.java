package ecommerce.platform.common.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrandTest {

    @Test
    @DisplayName("같은 브랜드끼리 equals는 true")
    void sameBrand() {
        assertThat(Brand.A.equals(Brand.A)).isTrue();
    }

    @Test
    @DisplayName("다른 브랜드끼리 equals는 false")
    void differentBrand() {
        assertThat(Brand.A.equals(Brand.B)).isFalse();
    }
}