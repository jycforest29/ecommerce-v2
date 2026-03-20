package ecommerce.platform.review.dto;

import ecommerce.platform.review.entity.Review;
import ecommerce.platform.review.entity.ReviewScore;

import java.time.Instant;

public record ReviewUpdateResponse(
        Long reviewId,
        Long imageId,
        String title,
        String content,
        ReviewScore score,
        Instant modifiedAt
) {
    public static ReviewUpdateResponse from(Review review) {
        return new ReviewUpdateResponse(
                review.getId(),
                review.getImageId(),
                review.getTitle(),
                review.getContent(),
                review.getScore(),
                review.getModifiedAt()
        );
    }
}