package ecommerce.platform.coupon.service;

import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.coupon.dto.ProductQueryResponse;
import ecommerce.platform.coupon.entity.Product;
import ecommerce.platform.coupon.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductRepository productRepository;

    public List<ProductQueryResponse> queryProducts() {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(ProductQueryResponse::from)
                .toList();
    }

    public ProductQueryResponse queryProduct(Long productId) {
        Product product = EntityFinder.findEntity(productRepository, productId);
        return ProductQueryResponse.from(product);
    }
}