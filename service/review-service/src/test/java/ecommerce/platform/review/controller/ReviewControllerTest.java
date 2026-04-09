package ecommerce.platform.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ecommerce.platform.common.annotations.LoginUserArgumentResolver;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.GlobalExceptionHandler;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.review.dto.*;
import ecommerce.platform.review.entity.ReviewScore;
import ecommerce.platform.review.service.ReviewManageService;
import ecommerce.platform.review.service.ReviewQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ReviewController reviewController;

    @Mock
    private ReviewManageService reviewManageService;

    @Mock
    private ReviewQueryService reviewQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of())
        );
    }

    @Nested
    @DisplayName("POST /api/v1/reviews")
    class CreateReview {

        @Test
        @DisplayName("리뷰 생성 성공 시 200 반환")
        void createSuccess() throws Exception {
            ReviewCreateRequest request = new ReviewCreateRequest(
                    1L, 10L, "좋은 상품", "만족합니다", ReviewScore.FIVE
            );
            ReviewCreateResponse response = new ReviewCreateResponse(
                    1L, 1L, ReviewScore.FIVE, Instant.now()
            );

            given(reviewManageService.createReview(eq(1L), any(ReviewCreateRequest.class)))
                    .willReturn(response);

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reviewId").value(1))
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.score").value("FIVE"));
        }

        @Test
        @DisplayName("구매 이력 없이 리뷰 작성 시 409 반환")
        void createWithoutPurchase() throws Exception {
            ReviewCreateRequest request = new ReviewCreateRequest(
                    1L, 10L, "좋은 상품", "만족합니다", ReviewScore.FIVE
            );

            given(reviewManageService.createReview(eq(1L), any(ReviewCreateRequest.class)))
                    .willThrow(new IllegalStateException("구매 이력이 없는 상품에는 리뷰를 작성할 수 없습니다."));

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("필수값 누락 시 400 반환")
        void createFail_validation() throws Exception {
            String invalidRequest = """
                    {
                        "productId": null,
                        "title": "",
                        "content": ""
                    }
                    """;

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/reviews/{reviewId}")
    class ModifyReview {

        @Test
        @DisplayName("리뷰 수정 성공 시 200 반환")
        void modifySuccess() throws Exception {
            ReviewUpdateRequest request = new ReviewUpdateRequest(
                    20L, "수정 제목", "수정 내용", ReviewScore.FOUR
            );
            ReviewUpdateResponse response = new ReviewUpdateResponse(
                    1L, 20L, "수정 제목", "수정 내용", ReviewScore.FOUR, Instant.now()
            );

            given(reviewManageService.modifyReview(eq(1L), eq(1L), any(ReviewUpdateRequest.class)))
                    .willReturn(response);

            mockMvc.perform(patch("/api/v1/reviews/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reviewId").value(1))
                    .andExpect(jsonPath("$.title").value("수정 제목"))
                    .andExpect(jsonPath("$.score").value("FOUR"));
        }

        @Test
        @DisplayName("본인 리뷰가 아니면 403 반환")
        void modifyForbidden() throws Exception {
            ReviewUpdateRequest request = new ReviewUpdateRequest(
                    20L, "수정 제목", "수정 내용", ReviewScore.FOUR
            );

            given(reviewManageService.modifyReview(eq(1L), eq(1L), any(ReviewUpdateRequest.class)))
                    .willThrow(new UnauthorizedAccessException());

            mockMvc.perform(patch("/api/v1/reviews/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reviews?productId=")
    class QueryReviews {

        @Test
        @DisplayName("상품별 리뷰 목록 조회 성공")
        void querySuccess() throws Exception {
            List<ReviewQueryResponse> responses = List.of(
                    ReviewQueryResponse.builder()
                            .reviewId(1L).productId(1L).userId(10L)
                            .title("좋아요").content("만족").score(ReviewScore.FIVE)
                            .createdAt(Instant.now()).build(),
                    ReviewQueryResponse.builder()
                            .reviewId(2L).productId(1L).userId(20L)
                            .title("보통").content("그럭저럭").score(ReviewScore.THREE)
                            .createdAt(Instant.now()).build()
            );

            given(reviewQueryService.queryReviews(1L)).willReturn(responses);

            mockMvc.perform(get("/api/v1/reviews").param("productId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].reviewId").value(1))
                    .andExpect(jsonPath("$[0].score").value("FIVE"))
                    .andExpect(jsonPath("$[1].reviewId").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reviews/{reviewId}")
    class QueryReview {

        @Test
        @DisplayName("단건 조회 성공")
        void querySuccess() throws Exception {
            ReviewQueryResponse response = ReviewQueryResponse.builder()
                    .reviewId(1L).productId(1L).userId(10L)
                    .title("좋아요").content("만족").score(ReviewScore.FIVE)
                    .createdAt(Instant.now()).build();

            given(reviewQueryService.queryReview(1L)).willReturn(response);

            mockMvc.perform(get("/api/v1/reviews/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reviewId").value(1))
                    .andExpect(jsonPath("$.title").value("좋아요"));
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 조회 시 404 반환")
        void queryNotFound() throws Exception {
            given(reviewQueryService.queryReview(999L)).willThrow(new EntityNotFoundException());

            mockMvc.perform(get("/api/v1/reviews/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/reviews/{reviewId}")
    class DeleteReview {

        @Test
        @DisplayName("리뷰 삭제 성공 시 200 반환")
        void deleteSuccess() throws Exception {
            mockMvc.perform(delete("/api/v1/reviews/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("본인 리뷰가 아니면 403 반환")
        void deleteForbidden() throws Exception {
            doThrow(new UnauthorizedAccessException())
                    .when(reviewManageService).deleteReview(1L, 1L);

            mockMvc.perform(delete("/api/v1/reviews/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 삭제 시 404 반환")
        void deleteNotFound() throws Exception {
            doThrow(new EntityNotFoundException())
                    .when(reviewManageService).deleteReview(1L, 999L);

            mockMvc.perform(delete("/api/v1/reviews/999"))
                    .andExpect(status().isNotFound());
        }
    }
}