package ecommerce.platform.coupon.service;

import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.coupon.dto.ProductQueryResponse;
import ecommerce.platform.coupon.entity.Product;
import ecommerce.platform.coupon.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductRepository productRepository;

    public Page<ProductQueryResponse> queryProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ProductQueryResponse::from);
    }

    public ProductQueryResponse queryProduct(Long productId) {
        Product product = EntityFinder.findEntity(productRepository, productId);
        return ProductQueryResponse.from(product);
    }
}