package ecommerce.platform.common.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    @DisplayName("같은 카테고리끼리 equals는 true")
    void sameCategory() {
        assertThat(Category.OUTER.equals(Category.OUTER)).isTrue();
    }

    @Test
    @DisplayName("다른 카테고리끼리 equals는 false")
    void differentCategory() {
        assertThat(Category.OUTER.equals(Category.SHOES)).isFalse();
    }
}