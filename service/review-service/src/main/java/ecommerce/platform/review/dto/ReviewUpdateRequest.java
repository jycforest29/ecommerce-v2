package ecommerce.platform.review.dto;

import ecommerce.platform.review.entity.ReviewScore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewUpdateRequest(
        Long imageId,
        @NotBlank String title,
        @NotBlank String content,
        @NotNull ReviewScore score
) {
}