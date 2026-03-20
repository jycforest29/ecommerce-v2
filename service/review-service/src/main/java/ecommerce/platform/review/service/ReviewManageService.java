package ecommerce.platform.review.service;

import ecommerce.platform.common.event.review.ReviewCreatedEvent;
import ecommerce.platform.common.event.review.ReviewDeletedEvent;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.review.dto.ReviewCreateRequest;
import ecommerce.platform.review.dto.ReviewCreateResponse;
import ecommerce.platform.review.dto.ReviewUpdateRequest;
import ecommerce.platform.review.dto.ReviewUpdateResponse;
import ecommerce.platform.review.entity.Review;
import ecommerce.platform.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewManageService {

    private final ReviewRepository reviewRepository;
    private final ReviewScoreUpdater reviewScoreUpdater;
    private final PurchaseVerifier purchaseVerifier;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public ReviewCreateResponse createReview(Long userId, ReviewCreateRequest request) {
        if (!purchaseVerifier.hasPurchased(request.productId(), userId)) {
            throw new IllegalStateException("구매 이력이 없는 상품에는 리뷰를 작성할 수 없습니다.");
        }

        Review review = request.toEntity(userId);
        reviewRepository.save(review);

        reviewScoreUpdater.addScore(review.getProductId(), review.getScore().getRateScore());
        kafkaTemplate.send(ReviewCreatedEvent.TOPIC, ReviewCreatedEvent.builder()
                .productId(review.getProductId())
                .averageScore(reviewScoreUpdater.getAverageScore(review.getProductId()))
                .build());

        return ReviewCreateResponse.from(review);
    }

    @Transactional
    public ReviewUpdateResponse modifyReview(Long userId, Long reviewId, ReviewUpdateRequest request) {
        Review review = EntityFinder.findEntity(reviewRepository, reviewId);
        validateOwner(review, userId);

        int oldScore = review.getScore().getRateScore();
        review.modify(request.imageId(), request.title(), request.content(), request.score());
        int newScore = request.score().getRateScore();

        reviewScoreUpdater.removeScore(review.getProductId(), oldScore);
        reviewScoreUpdater.addScore(review.getProductId(), newScore);

        return ReviewUpdateResponse.from(review);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = EntityFinder.findEntity(reviewRepository, reviewId);
        validateOwner(review, userId);
        review.delete();

        reviewScoreUpdater.removeScore(review.getProductId(), review.getScore().getRateScore());
        kafkaTemplate.send(ReviewDeletedEvent.TOPIC, ReviewDeletedEvent.builder()
                .productId(review.getProductId())
                .averageScore(reviewScoreUpdater.getAverageScore(review.getProductId()))
                .build());
    }

    private void validateOwner(Review review, Long userId) {
        if (!review.writtenBy(userId)) {
            throw new UnauthorizedAccessException();
        }
    }
}
