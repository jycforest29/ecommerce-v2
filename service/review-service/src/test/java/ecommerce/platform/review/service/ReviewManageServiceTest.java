package ecommerce.platform.review.service;

import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.review.dto.ReviewCreateRequest;
import ecommerce.platform.review.dto.ReviewCreateResponse;
import ecommerce.platform.review.dto.ReviewUpdateRequest;
import ecommerce.platform.review.dto.ReviewUpdateResponse;
import ecommerce.platform.review.entity.Review;
import ecommerce.platform.review.entity.ReviewScore;
import ecommerce.platform.review.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ReviewManageServiceTest {

    @InjectMocks
    private ReviewManageService reviewManageService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewScoreUpdater reviewScoreUpdater;

    @Mock
    private PurchaseVerifier purchaseVerifier;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private Review createReview(Long id, Long productId, Long userId, ReviewScore score) {
        Review review = Review.builder()
                .productId(productId)
                .userId(userId)
                .imageId(10L)
                .title("좋은 상품")
                .content("만족합니다")
                .score(score)
                .build();
        ReflectionTestUtils.setField(review, "id", id);
        return review;
    }

    @Nested
    @DisplayName("리뷰 생성 - createReview")
    class CreateReview {

        @Test
        @DisplayName("구매 이력이 있으면 리뷰를 생성하고 Kafka 이벤트를 발행한다")
        void createReviewSuccess() {
            ReviewCreateRequest request = new ReviewCreateRequest(
                    1L, 10L, "좋은 상품", "만족합니다", ReviewScore.FIVE
            );
            given(purchaseVerifier.hasPurchased(1L, 1L)).willReturn(true);
            given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> {
                Review saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 1L);
                return saved;
            });
            given(reviewScoreUpdater.getAverageScore(1L)).willReturn(5);

            ReviewCreateResponse response = reviewManageService.createReview(1L, request);

            assertThat(response.reviewId()).isEqualTo(1L);
            assertThat(response.productId()).isEqualTo(1L);
            assertThat(response.score()).isEqualTo(ReviewScore.FIVE);
            then(reviewScoreUpdater).should().addScore(1L, 5);
            then(kafkaTemplate).should().send(eq("review.events.created"), any());
        }

        @Test
        @DisplayName("구매 이력이 없으면 예외가 발생한다")
        void createReviewWithoutPurchase() {
            ReviewCreateRequest request = new ReviewCreateRequest(
                    1L, 10L, "좋은 상품", "만족합니다", ReviewScore.FIVE
            );
            given(purchaseVerifier.hasPurchased(1L, 1L)).willReturn(false);

            assertThatThrownBy(() -> reviewManageService.createReview(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("구매 이력이 없는 상품에는 리뷰를 작성할 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("리뷰 수정 - modifyReview")
    class ModifyReview {

        @Test
        @DisplayName("본인의 리뷰를 수정하면 점수가 갱신된다")
        void modifyReviewSuccess() {
            Review review = createReview(1L, 1L, 1L, ReviewScore.THREE);
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            ReviewUpdateRequest request = new ReviewUpdateRequest(20L, "수정 제목", "수정 내용", ReviewScore.FIVE);

            ReviewUpdateResponse response = reviewManageService.modifyReview(1L, 1L, request);

            assertThat(response.reviewId()).isEqualTo(1L);
            assertThat(response.score()).isEqualTo(ReviewScore.FIVE);
            then(reviewScoreUpdater).should().removeScore(1L, 3);
            then(reviewScoreUpdater).should().addScore(1L, 5);
        }

        @Test
        @DisplayName("본인의 리뷰가 아니면 예외가 발생한다")
        void modifyReviewUnauthorized() {
            Review review = createReview(1L, 1L, 1L, ReviewScore.THREE);
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            ReviewUpdateRequest request = new ReviewUpdateRequest(20L, "수정", "내용", ReviewScore.FIVE);

            assertThatThrownBy(() -> reviewManageService.modifyReview(999L, 1L, request))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    @Nested
    @DisplayName("리뷰 삭제 - deleteReview")
    class DeleteReview {

        @Test
        @DisplayName("본인의 리뷰를 삭제하면 소프트 삭제되고 Kafka 이벤트를 발행한다")
        void deleteReviewSuccess() {
            Review review = createReview(1L, 1L, 1L, ReviewScore.FOUR);
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));
            given(reviewScoreUpdater.getAverageScore(1L)).willReturn(3);

            reviewManageService.deleteReview(1L, 1L);

            assertThat(review.isDeleted()).isTrue();
            then(reviewScoreUpdater).should().removeScore(1L, 4);
            then(kafkaTemplate).should().send(eq("review.events.deleted"), any());
        }

        @Test
        @DisplayName("본인의 리뷰가 아니면 예외가 발생한다")
        void deleteReviewUnauthorized() {
            Review review = createReview(1L, 1L, 1L, ReviewScore.FOUR);
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            assertThatThrownBy(() -> reviewManageService.deleteReview(999L, 1L))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }
}