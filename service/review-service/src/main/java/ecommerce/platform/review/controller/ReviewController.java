package ecommerce.platform.review.controller;

import ecommerce.platform.common.annotations.Login;
import ecommerce.platform.review.dto.ReviewCreateRequest;
import ecommerce.platform.review.dto.ReviewCreateResponse;
import ecommerce.platform.review.dto.ReviewQueryResponse;
import ecommerce.platform.review.dto.ReviewUpdateRequest;
import ecommerce.platform.review.dto.ReviewUpdateResponse;
import ecommerce.platform.review.service.ReviewManageService;
import ecommerce.platform.review.service.ReviewQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@RestController
public class ReviewController {

    private final ReviewManageService reviewManageService;
    private final ReviewQueryService reviewQueryService;

    @PostMapping
    public ResponseEntity<ReviewCreateResponse> createReview(
            @Login Long userId,
            @Valid @RequestBody ReviewCreateRequest reviewCreateRequest) {
        ReviewCreateResponse response = reviewManageService.createReview(userId, reviewCreateRequest);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewUpdateResponse> modifyReview(
            @Login Long userId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest reviewUpdateRequest) {
        ReviewUpdateResponse response = reviewManageService.modifyReview(userId, reviewId, reviewUpdateRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ReviewQueryResponse>> queryReviews(@RequestParam Long productId) {
        List<ReviewQueryResponse> responses = reviewQueryService.queryReviews(productId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewQueryResponse> queryReview(@PathVariable Long reviewId) {
        ReviewQueryResponse response = reviewQueryService.queryReview(reviewId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Login Long userId,
            @PathVariable Long reviewId) {
        reviewManageService.deleteReview(userId, reviewId);
        return ResponseEntity.ok().build();
    }
}