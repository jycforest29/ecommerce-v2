package ecommerce.platform.coupon.entity;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    private Product createProduct(Long sellerId) {
        return Product.builder()
                .sellerId(sellerId)
                .name("테스트 상품")
                .imageId(1L)
                .brand(Brand.A)
                .category(Category.OUTER)
                .price(50000)
                .description("테스트 상품 설명")
                .build();
    }

    @Nested
    @DisplayName("상품 생성")
    class Create {

        @Test
        @DisplayName("빌더로 상품을 생성하면 기본값이 올바르게 설정된다")
        void createProductWithDefaults() {
            Product product = createProduct(1L);

            assertThat(product.getSellerId()).isEqualTo(1L);
            assertThat(product.getName()).isEqualTo("테스트 상품");
            assertThat(product.getImageId()).isEqualTo(1L);
            assertThat(product.getBrand()).isEqualTo(Brand.A);
            assertThat(product.getCategory()).isEqualTo(Category.OUTER);
            assertThat(product.getPrice()).isEqualTo(50000);
            assertThat(product.getDescription()).isEqualTo("테스트 상품 설명");
            assertThat(product.isInSale()).isTrue();
            assertThat(product.getReviewCount()).isZero();
            assertThat(product.getCreatedAt()).isNotNull();
            assertThat(product.getModifiedAt()).isNull();
            assertThat(product.getDeletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("상품 수정")
    class Modify {

        @Test
        @DisplayName("modify 호출 시 imageId와 price가 변경된다")
        void modifyUpdatesFields() {
            Product product = createProduct(1L);

            product.modify(2L, 60000);

            assertThat(product.getImageId()).isEqualTo(2L);
            assertThat(product.getPrice()).isEqualTo(60000);
            assertThat(product.getModifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("modify 호출 시 다른 필드는 변경되지 않는다")
        void modifyDoesNotAffectOtherFields() {
            Product product = createProduct(1L);

            product.modify(2L, 60000);

            assertThat(product.getSellerId()).isEqualTo(1L);
            assertThat(product.getName()).isEqualTo("테스트 상품");
            assertThat(product.getBrand()).isEqualTo(Brand.A);
            assertThat(product.getCategory()).isEqualTo(Category.OUTER);
            assertThat(product.isInSale()).isTrue();
        }
    }

    @Nested
    @DisplayName("상품 삭제 (소프트 삭제)")
    class Delete {

        @Test
        @DisplayName("delete 호출 시 deletedAt이 설정되고 inSale이 false가 된다")
        void deleteSetsSoftDeleteFields() {
            Product product = createProduct(1L);

            product.delete();

            assertThat(product.getDeletedAt()).isNotNull();
            assertThat(product.isInSale()).isFalse();
        }

        @Test
        @DisplayName("delete 호출 시 다른 필드는 변경되지 않는다")
        void deleteDoesNotAffectOtherFields() {
            Product product = createProduct(1L);

            product.delete();

            assertThat(product.getSellerId()).isEqualTo(1L);
            assertThat(product.getName()).isEqualTo("테스트 상품");
            assertThat(product.getPrice()).isEqualTo(50000);
        }
    }
}
