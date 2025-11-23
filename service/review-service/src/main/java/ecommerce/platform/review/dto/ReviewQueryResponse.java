package ecommerce.platform.review.dto;

import ecommerce.platform.review.entity.Review;
import ecommerce.platform.review.entity.ReviewScore;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ReviewQueryResponse(
        Long reviewId,
        Long productId,
        Long userId,
        Long imageId,
        String title,
        String content,
        ReviewScore score,
        Instant createdAt,
        Instant modifiedAt
) {
    public static ReviewQueryResponse from(Review review) {
        return ReviewQueryResponse.builder()
                .reviewId(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .imageId(review.getImageId())
                .title(review.getTitle())
                .content(review.getContent())
                .score(review.getScore())
                .createdAt(review.getCreatedAt())
                .modifiedAt(review.getModifiedAt())
                .build();
    }
}