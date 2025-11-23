package ecommerce.platform.review.dto;

import ecommerce.platform.review.entity.Review;
import ecommerce.platform.review.entity.ReviewScore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
        @NotNull Long productId,
        Long imageId,
        @NotBlank String title,
        @NotBlank String content,
        @NotNull ReviewScore score
) {
    public Review toEntity(Long userId) {
        return Review.builder()
                .productId(productId)
                .userId(userId)
                .imageId(imageId)
                .title(title)
                .content(content)
                .score(score)
                .build();
    }
}