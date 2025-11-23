package ecommerce.platform.coupon.dto;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.coupon.entity.Product;

import java.time.Instant;

public record ProductCreateResponse(
        Long productId,
        String name,
        Brand brand,
        Category category,
        int price,
        Instant createdAt
) {
    public static ProductCreateResponse from(Product product) {
        return new ProductCreateResponse(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getCategory(),
                product.getPrice(),
                product.getCreatedAt()
        );
    }
}