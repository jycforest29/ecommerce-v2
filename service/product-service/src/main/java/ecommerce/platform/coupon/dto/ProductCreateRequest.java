package ecommerce.platform.coupon.dto;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.coupon.entity.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductCreateRequest(
        @NotBlank String name,
        Long imageId,
        @NotNull Brand brand,
        @NotNull Category category,
        @Positive int price,
        @NotBlank String description
) {
    public Product toEntity(Long sellerId) {
        return Product.builder()
                .sellerId(sellerId)
                .name(name)
                .imageId(imageId)
                .brand(brand)
                .category(category)
                .price(price)
                .description(description)
                .build();
    }
}