package ecommerce.platform.coupon.service;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.coupon.dto.ProductQueryResponse;
import ecommerce.platform.coupon.entity.Product;
import ecommerce.platform.coupon.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductQueryServiceTest {

    @InjectMocks
    private ProductQueryService productQueryService;

    @Mock
    private ProductRepository productRepository;

    private Product createProduct(Long id, String name, Brand brand, Category category, int price) {
        Product product = Product.builder()
                .sellerId(1L)
                .name(name)
                .imageId(1L)
                .brand(brand)
                .category(category)
                .price(price)
                .description("설명")
                .build();
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    @Nested
    @DisplayName("상품 목록 조회 - queryProducts")
    class QueryProducts {

        @Test
        @DisplayName("전체 상품 목록을 조회한다")
        void queryAllProducts() {
            List<Product> products = List.of(
                    createProduct(1L, "상품A", Brand.A, Category.OUTER, 50000),
                    createProduct(2L, "상품B", Brand.B, Category.SHOES, 80000)
            );
            given(productRepository.findAll()).willReturn(products);

            List<ProductQueryResponse> responses = productQueryService.queryProducts();

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).productId()).isEqualTo(1L);
            assertThat(responses.get(0).name()).isEqualTo("상품A");
            assertThat(responses.get(1).productId()).isEqualTo(2L);
            assertThat(responses.get(1).name()).isEqualTo("상품B");
        }

        @Test
        @DisplayName("상품이 없으면 빈 리스트를 반환한다")
        void queryProductsEmpty() {
            given(productRepository.findAll()).willReturn(List.of());

            List<ProductQueryResponse> responses = productQueryService.queryProducts();

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("단건 상품 조회 - queryProduct")
    class QueryProduct {

        @Test
        @DisplayName("상품 ID로 단건 조회한다")
        void queryProductById() {
            Product product = createProduct(1L, "상품A", Brand.A, Category.OUTER, 50000);
            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            ProductQueryResponse response = productQueryService.queryProduct(1L);

            assertThat(response.productId()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("상품A");
            assertThat(response.brand()).isEqualTo(Brand.A);
            assertThat(response.category()).isEqualTo(Category.OUTER);
            assertThat(response.price()).isEqualTo(50000);
            assertThat(response.inSale()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 조회하면 예외가 발생한다")
        void queryProductNotFound() {
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productQueryService.queryProduct(999L))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
