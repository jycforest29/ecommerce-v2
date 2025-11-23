package ecommerce.platform.coupon.dto;

import ecommerce.platform.coupon.entity.Product;

import java.time.Instant;

public record ProductUpdateResponse(
        Long productId,
        Long imageId,
        int price,
        Instant modifiedAt
) {
    public static ProductUpdateResponse from(Product product) {
        return new ProductUpdateResponse(
                product.getId(),
                product.getImageId(),
                product.getPrice(),
                product.getModifiedAt()
        );
    }
}