package ecommerce.platform.review.dto;

import ecommerce.platform.review.entity.Review;
import ecommerce.platform.review.entity.ReviewScore;

import java.time.Instant;

public record ReviewCreateResponse(
        Long reviewId,
        Long productId,
        ReviewScore score,
        Instant createdAt
) {
    public static ReviewCreateResponse from(Review review) {
        return new ReviewCreateResponse(
                review.getId(),
                review.getProductId(),
                review.getScore(),
                review.getCreatedAt()
        );
    }
}