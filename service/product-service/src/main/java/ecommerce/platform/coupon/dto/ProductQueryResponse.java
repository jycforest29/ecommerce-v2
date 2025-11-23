package ecommerce.platform.coupon.dto;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.coupon.entity.Product;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ProductQueryResponse(
        Long productId,
        Long sellerId,
        String name,
        Long imageId,
        Brand brand,
        Category category,
        int price,
        String description,
        int reviewCount,
        boolean inSale,
        Instant createdAt,
        Instant modifiedAt
) {
    public static ProductQueryResponse from(Product product) {
        return ProductQueryResponse.builder()
                .productId(product.getId())
                .sellerId(product.getSellerId())
                .name(product.getName())
                .imageId(product.getImageId())
                .brand(product.getBrand())
                .category(product.getCategory())
                .price(product.getPrice())
                .description(product.getDescription())
                .reviewCount(product.getReviewCount())
                .inSale(product.isInSale())
                .createdAt(product.getCreatedAt())
                .modifiedAt(product.getModifiedAt())
                .build();
    }
}