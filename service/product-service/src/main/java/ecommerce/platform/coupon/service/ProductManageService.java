package ecommerce.platform.coupon.service;

import ecommerce.platform.common.event.product.StockDeductRequestEvent;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.coupon.dto.ProductCreateRequest;
import ecommerce.platform.coupon.dto.ProductCreateResponse;
import ecommerce.platform.coupon.dto.ProductUpdateRequest;
import ecommerce.platform.coupon.dto.ProductUpdateResponse;
import ecommerce.platform.coupon.entity.Product;
import ecommerce.platform.coupon.entity.ProductOption;
import ecommerce.platform.coupon.repository.ProductOptionRepository;
import ecommerce.platform.coupon.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductManageService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;

    @Transactional
    public ProductCreateResponse createProduct(Long userId, ProductCreateRequest productCreateRequest) {
        Product product = productCreateRequest.toEntity(userId);
        productRepository.save(product);
        return ProductCreateResponse.from(product);
    }

    @Transactional
    public ProductUpdateResponse modifyProduct(Long productId, Long userId, ProductUpdateRequest productUpdateRequest) {
        Product product = EntityFinder.findEntity(productRepository, productId);
        validateOwner(product, userId);
        product.modify(productUpdateRequest.imageId(), productUpdateRequest.price());
        return ProductUpdateResponse.from(product);
    }

    @Transactional
    public void deleteProduct(Long productId, Long userId) {
        Product product = EntityFinder.findEntity(productRepository, productId);
        validateOwner(product, userId);
        product.delete();
    }

    @Transactional
    public void deductStock(Long orderId, List<StockDeductRequestEvent.StockInfo> stockInfos) {
        for (StockDeductRequestEvent.StockInfo stockInfo : stockInfos) {
            int updatedRows = productOptionRepository.deductStockConditionally(
                    stockInfo.getOptionId(), stockInfo.getQuantity());
            if (updatedRows == 0) {
                throw new IllegalStateException(
                        "재고 차감 실패 - optionId: " + stockInfo.getOptionId() + ", quantity: " + stockInfo.getQuantity());
            }
        }
    }

    @Transactional
    public void updateReviewCount(Long productId, int reviewDelta) {
        productRepository.updateReviewCountByProductId(productId, reviewDelta);
    }

    private void validateOwner(Product product, Long userId) {
        if (!product.getSellerId().equals(userId)) {
            throw new UnauthorizedAccessException();
        }
    }
}