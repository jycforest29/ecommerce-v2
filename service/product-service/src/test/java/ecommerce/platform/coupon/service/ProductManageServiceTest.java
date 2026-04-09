package ecommerce.platform.coupon.service;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.common.constants.ClothingSize;
import ecommerce.platform.common.constants.Color;
import ecommerce.platform.common.event.product.StockDeductRequestEvent;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.coupon.dto.ProductCreateRequest;
import ecommerce.platform.coupon.dto.ProductCreateResponse;
import ecommerce.platform.coupon.dto.ProductUpdateRequest;
import ecommerce.platform.coupon.dto.ProductUpdateResponse;
import ecommerce.platform.coupon.entity.Product;
import ecommerce.platform.coupon.entity.ProductOption;
import ecommerce.platform.coupon.repository.ProductOptionRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ProductManageServiceTest {

    @InjectMocks
    private ProductManageService productManageService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    private Product createProduct(Long id, Long sellerId) {
        Product product = Product.builder()
                .sellerId(sellerId)
                .name("테스트 상품")
                .imageId(1L)
                .brand(Brand.A)
                .category(Category.OUTER)
                .price(50000)
                .description("테스트 상품 설명")
                .build();
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private ProductOption createOption(Long id, Product product, int stock) {
        ProductOption option = ProductOption.builder()
                .product(product)
                .clothingSize(ClothingSize.M)
                .color(Color.BLACK)
                .additionalPrice(5000)
                .stock(stock)
                .build();
        ReflectionTestUtils.setField(option, "id", id);
        return option;
    }

    @Nested
    @DisplayName("상품 생성 - createProduct")
    class CreateProduct {

        @Test
        @DisplayName("상품이 정상적으로 생성된다")
        void createProductSuccess() {
            ProductCreateRequest request = new ProductCreateRequest(
                    "테스트 상품", 1L, Brand.A, Category.OUTER, 50000, "테스트 설명"
            );
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 1L);
                return saved;
            });

            ProductCreateResponse response = productManageService.createProduct(1L, request);

            assertThat(response.productId()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("테스트 상품");
            assertThat(response.brand()).isEqualTo(Brand.A);
            assertThat(response.category()).isEqualTo(Category.OUTER);
            assertThat(response.price()).isEqualTo(50000);
            then(productRepository).should().save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("상품 수정 - modifyProduct")
    class ModifyProduct {

        @Test
        @DisplayName("본인의 상품을 수정하면 성공한다")
        void modifyProductSuccess() {
            Long productId = 1L;
            Long sellerId = 1L;
            Product product = createProduct(productId, sellerId);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            ProductUpdateRequest request = new ProductUpdateRequest(2L, 60000);

            ProductUpdateResponse response = productManageService.modifyProduct(productId, sellerId, request);

            assertThat(response.productId()).isEqualTo(productId);
            assertThat(response.imageId()).isEqualTo(2L);
            assertThat(response.price()).isEqualTo(60000);
            assertThat(response.modifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("본인의 상품이 아니면 수정 시 예외가 발생한다")
        void modifyProductUnauthorized() {
            Long productId = 1L;
            Product product = createProduct(productId, 1L);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            ProductUpdateRequest request = new ProductUpdateRequest(2L, 60000);

            assertThatThrownBy(() -> productManageService.modifyProduct(productId, 999L, request))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    @Nested
    @DisplayName("상품 삭제 - deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("본인의 상품을 삭제하면 소프트 삭제된다")
        void deleteProductSuccess() {
            Long productId = 1L;
            Long sellerId = 1L;
            Product product = createProduct(productId, sellerId);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            productManageService.deleteProduct(productId, sellerId);

            assertThat(product.isInSale()).isFalse();
            assertThat(product.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("본인의 상품이 아니면 삭제 시 예외가 발생한다")
        void deleteProductUnauthorized() {
            Long productId = 1L;
            Product product = createProduct(productId, 1L);
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            assertThatThrownBy(() -> productManageService.deleteProduct(productId, 999L))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    @Nested
    @DisplayName("재고 차감 - deductStock")
    class DeductStock {

        @Test
        @DisplayName("옵션 재고를 차감한다")
        void deductStockSuccess() {
            Product product = createProduct(1L, 1L);
            ProductOption option = createOption(10L, product, 100);
            given(productOptionRepository.findById(10L)).willReturn(Optional.of(option));

            StockDeductRequestEvent.StockInfo stockInfo = new StockDeductRequestEvent.StockInfo(1L, 10L, 3);

            productManageService.deductStock(1L, List.of(stockInfo));

            assertThat(option.getStock()).isEqualTo(97);
        }

        @Test
        @DisplayName("여러 옵션의 재고를 한번에 차감한다")
        void deductStockMultipleOptions() {
            Product product = createProduct(1L, 1L);
            ProductOption option1 = createOption(10L, product, 100);
            ProductOption option2 = createOption(20L, product, 50);
            given(productOptionRepository.findById(10L)).willReturn(Optional.of(option1));
            given(productOptionRepository.findById(20L)).willReturn(Optional.of(option2));

            List<StockDeductRequestEvent.StockInfo> stockInfos = List.of(
                    new StockDeductRequestEvent.StockInfo(1L, 10L, 5),
                    new StockDeductRequestEvent.StockInfo(1L, 20L, 10)
            );

            productManageService.deductStock(1L, stockInfos);

            assertThat(option1.getStock()).isEqualTo(95);
            assertThat(option2.getStock()).isEqualTo(40);
        }

        @Test
        @DisplayName("재고가 부족하면 예외가 발생한다")
        void deductStockInsufficientThrowsException() {
            Product product = createProduct(1L, 1L);
            ProductOption option = createOption(10L, product, 2);
            given(productOptionRepository.findById(10L)).willReturn(Optional.of(option));

            StockDeductRequestEvent.StockInfo stockInfo = new StockDeductRequestEvent.StockInfo(1L, 10L, 5);

            assertThatThrownBy(() -> productManageService.deductStock(1L, List.of(stockInfo)))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("리뷰 수 갱신 - updateReviewCount")
    class UpdateReviewCount {

        @Test
        @DisplayName("리뷰 생성 시 리뷰 수를 1 증가시킨다")
        void updateReviewCountIncrement() {
            productManageService.updateReviewCount(1L, 1);

            then(productRepository).should().updateReviewCountByProductId(1L, 1);
        }

        @Test
        @DisplayName("리뷰 삭제 시 리뷰 수를 1 감소시킨다")
        void updateReviewCountDecrement() {
            productManageService.updateReviewCount(1L, -1);

            then(productRepository).should().updateReviewCountByProductId(1L, -1);
        }
    }
}
