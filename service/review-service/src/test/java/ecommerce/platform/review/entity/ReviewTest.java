package ecommerce.platform.review.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewTest {

    private Review createReview(Long userId) {
        return Review.builder()
                .productId(1L)
                .userId(userId)
                .imageId(10L)
                .title("좋은 상품")
                .content("만족합니다")
                .score(ReviewScore.FIVE)
                .build();
    }

    @Nested
    @DisplayName("리뷰 생성")
    class Create {

        @Test
        @DisplayName("빌더로 리뷰를 생성하면 기본값이 올바르게 설정된다")
        void createReviewWithDefaults() {
            Review review = createReview(1L);

            assertThat(review.getProductId()).isEqualTo(1L);
            assertThat(review.getUserId()).isEqualTo(1L);
            assertThat(review.getImageId()).isEqualTo(10L);
            assertThat(review.getTitle()).isEqualTo("좋은 상품");
            assertThat(review.getContent()).isEqualTo("만족합니다");
            assertThat(review.getScore()).isEqualTo(ReviewScore.FIVE);
            assertThat(review.getCreatedAt()).isNotNull();
            assertThat(review.getModifiedAt()).isNull();
            assertThat(review.getDeletedAt()).isNull();
            assertThat(review.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("리뷰 수정")
    class Modify {

        @Test
        @DisplayName("modify 호출 시 필드가 변경되고 modifiedAt이 설정된다")
        void modifyUpdatesFields() {
            Review review = createReview(1L);

            review.modify(20L, "수정된 제목", "수정된 내용", ReviewScore.THREE);

            assertThat(review.getImageId()).isEqualTo(20L);
            assertThat(review.getTitle()).isEqualTo("수정된 제목");
            assertThat(review.getContent()).isEqualTo("수정된 내용");
            assertThat(review.getScore()).isEqualTo(ReviewScore.THREE);
            assertThat(review.getModifiedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("작성자 확인 - writtenBy")
    class WrittenBy {

        @Test
        @DisplayName("작성자 본인이면 true를 반환한다")
        void writtenByOwner() {
            Review review = createReview(1L);

            assertThat(review.writtenBy(1L)).isTrue();
        }

        @Test
        @DisplayName("작성자가 아니면 false를 반환한다")
        void notWrittenByOther() {
            Review review = createReview(1L);

            assertThat(review.writtenBy(999L)).isFalse();
        }
    }

    @Nested
    @DisplayName("리뷰 삭제 (소프트 삭제)")
    class Delete {

        @Test
        @DisplayName("delete 호출 시 deletedAt이 설정되고 isDeleted가 true가 된다")
        void deleteSetsDeletedAt() {
            Review review = createReview(1L);

            review.delete();

            assertThat(review.getDeletedAt()).isNotNull();
            assertThat(review.isDeleted()).isTrue();
        }
    }
}
