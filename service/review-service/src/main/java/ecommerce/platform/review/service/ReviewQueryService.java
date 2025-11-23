package ecommerce.platform.review.service;

import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.review.dto.ReviewQueryResponse;
import ecommerce.platform.review.entity.Review;
import ecommerce.platform.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;

    public List<ReviewQueryResponse> queryReviews(Long productId) {
        List<Review> reviews = reviewRepository.findAllByProductId(productId);

        return reviews.stream()
                .filter(review -> !review.isDeleted())
                .map(ReviewQueryResponse::from)
                .toList();
    }

    public ReviewQueryResponse queryReview(Long reviewId) {
        Review review = EntityFinder.findEntity(reviewRepository, reviewId);
        return ReviewQueryResponse.from(review);
    }
}