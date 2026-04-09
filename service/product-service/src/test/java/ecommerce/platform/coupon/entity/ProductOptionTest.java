package ecommerce.platform.coupon.entity;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.common.constants.ClothingSize;
import ecommerce.platform.common.constants.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductOptionTest {

    private Product createProduct() {
        return Product.builder()
                .sellerId(1L)
                .name("테스트 상품")
                .imageId(1L)
                .brand(Brand.A)
                .category(Category.OUTER)
                .price(50000)
                .description("테스트 상품 설명")
                .build();
    }

    private ProductOption createOption(int stock) {
        return ProductOption.builder()
                .product(createProduct())
                .clothingSize(ClothingSize.M)
                .color(Color.BLACK)
                .additionalPrice(5000)
                .stock(stock)
                .build();
    }

    @Nested
    @DisplayName("옵션 생성")
    class Create {

        @Test
        @DisplayName("빌더로 옵션을 생성하면 available이 true로 설정된다")
        void createOptionWithDefaults() {
            ProductOption option = createOption(10);

            assertThat(option.getProduct()).isNotNull();
            assertThat(option.getClothingSize()).isEqualTo(ClothingSize.M);
            assertThat(option.getColor()).isEqualTo(Color.BLACK);
            assertThat(option.getAdditionalPrice()).isEqualTo(5000);
            assertThat(option.getStock()).isEqualTo(10);
            assertThat(option.isAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("재고 차감 - deductStock")
    class DeductStock {

        @Test
        @DisplayName("재고가 충분하면 차감에 성공한다")
        void deductStockSuccess() {
            ProductOption option = createOption(10);

            option.deductStock(3);

            assertThat(option.getStock()).isEqualTo(7);
            assertThat(option.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("재고를 전부 차감하면 available이 false가 된다")
        void deductAllStockMakesUnavailable() {
            ProductOption option = createOption(5);

            option.deductStock(5);

            assertThat(option.getStock()).isZero();
            assertThat(option.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("재고보다 많은 수량을 차감하면 예외가 발생한다")
        void deductStockInsufficientThrowsException() {
            ProductOption option = createOption(3);

            assertThatThrownBy(() -> option.deductStock(5))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("재고가 부족합니다.");
        }

        @Test
        @DisplayName("재고가 0인 상태에서 차감하면 예외가 발생한다")
        void deductStockFromZeroThrowsException() {
            ProductOption option = createOption(0);

            assertThatThrownBy(() -> option.deductStock(1))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("재고 복구 - restoreStock")
    class RestoreStock {

        @Test
        @DisplayName("재고를 복구하면 수량이 증가한다")
        void restoreStockIncreasesQuantity() {
            ProductOption option = createOption(5);
            option.deductStock(5);

            option.restoreStock(3);

            assertThat(option.getStock()).isEqualTo(3);
            assertThat(option.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("available이 false인 상태에서 복구하면 true로 변경된다")
        void restoreStockMakesAvailable() {
            ProductOption option = createOption(5);
            option.deductStock(5);
            assertThat(option.isAvailable()).isFalse();

            option.restoreStock(1);

            assertThat(option.isAvailable()).isTrue();
        }
    }
}
