package ecommerce.platform.review.service;

import ecommerce.platform.review.dto.ReviewQueryResponse;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReviewQueryServiceTest {

    @InjectMocks
    private ReviewQueryService reviewQueryService;

    @Mock
    private ReviewRepository reviewRepository;

    private Review createReview(Long id, Long productId, Long userId) {
        Review review = Review.builder()
                .productId(productId)
                .userId(userId)
                .imageId(10L)
                .title("좋은 상품")
                .content("만족합니다")
                .score(ReviewScore.FIVE)
                .build();
        ReflectionTestUtils.setField(review, "id", id);
        return review;
    }

    @Nested
    @DisplayName("상품별 리뷰 조회 - queryReviews")
    class QueryReviews {

        @Test
        @DisplayName("상품의 리뷰 목록을 조회한다")
        void queryReviewsSuccess() {
            List<Review> reviews = List.of(
                    createReview(1L, 1L, 10L),
                    createReview(2L, 1L, 20L)
            );
            given(reviewRepository.findAllByProductId(1L)).willReturn(reviews);

            List<ReviewQueryResponse> responses = reviewQueryService.queryReviews(1L);

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).reviewId()).isEqualTo(1L);
            assertThat(responses.get(1).reviewId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("삭제된 리뷰는 조회 결과에서 제외된다")
        void queryReviewsExcludesDeleted() {
            Review active = createReview(1L, 1L, 10L);
            Review deleted = createReview(2L, 1L, 20L);
            deleted.delete();
            given(reviewRepository.findAllByProductId(1L)).willReturn(List.of(active, deleted));

            List<ReviewQueryResponse> responses = reviewQueryService.queryReviews(1L);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).reviewId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("리뷰가 없으면 빈 리스트를 반환한다")
        void queryReviewsEmpty() {
            given(reviewRepository.findAllByProductId(1L)).willReturn(List.of());

            List<ReviewQueryResponse> responses = reviewQueryService.queryReviews(1L);

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("단건 리뷰 조회 - queryReview")
    class QueryReview {

        @Test
        @DisplayName("리뷰 ID로 단건 조회한다")
        void queryReviewSuccess() {
            Review review = createReview(1L, 1L, 10L);
            given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

            ReviewQueryResponse response = reviewQueryService.queryReview(1L);

            assertThat(response.reviewId()).isEqualTo(1L);
            assertThat(response.productId()).isEqualTo(1L);
            assertThat(response.score()).isEqualTo(ReviewScore.FIVE);
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 조회 시 예외가 발생한다")
        void queryReviewNotFound() {
            given(reviewRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reviewQueryService.queryReview(999L))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
