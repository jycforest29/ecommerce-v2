package ecommerce.platform.coupon.controller;

import ecommerce.platform.common.annotations.Login;
import ecommerce.platform.coupon.dto.CouponApplyRequest;
import ecommerce.platform.coupon.dto.CouponApplyResponse;
import ecommerce.platform.coupon.dto.CouponIssueResponse;
import ecommerce.platform.coupon.dto.CouponQueryResponse;
import ecommerce.platform.coupon.service.CouponApplyService;
import ecommerce.platform.coupon.service.CouponIssueService;
import ecommerce.platform.coupon.service.CouponQueryService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@RestController
public class CouponController {

    private final CouponApplyService couponApplyService;
    private final CouponIssueService couponIssueService;
    private final CouponQueryService couponQueryService;

    // WHAT: 쿠폰 생성
    @PostMapping("/issue/{promotionId}")
    public ResponseEntity<CouponIssueResponse> issuePromotion(@PathVariable Long promotionId, @Login Long userId) {
        CouponIssueResponse couponIssueResponse = couponIssueService.issuePromotion(promotionId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(couponIssueResponse);
    }

    // WHAT: 현재 유저가 가진 모든 쿠폰 조회
    @GetMapping
    public ResponseEntity<Page<CouponQueryResponse>> getIssuedCoupons(@Login Long userId, Pageable pageable) {
        Page<CouponQueryResponse> couponQueryResponses = couponQueryService.getAllIssuedCoupons(userId, pageable);
        return ResponseEntity.ok(couponQueryResponses);
    }

    // WHAT: 쿠폰의 상세정보 조회
    @GetMapping("/{couponId}")
    public ResponseEntity<CouponQueryResponse> getIssuedCoupon(@Login Long userId, @PathVariable Long couponId) {
        CouponQueryResponse couponQueryResponse = couponQueryService.getIssuedCoupon(userId, couponId);
        return ResponseEntity.ok(couponQueryResponse);
    }

    // WHAT: 쿠폰 사용
    @PatchMapping("/{couponId}/apply")
    public ResponseEntity<CouponApplyResponse> applyCoupon(@PathVariable Long couponId, @Valid @RequestBody CouponApplyRequest couponApplyRequest, @Login Long userId) {
        CouponApplyResponse couponApplyResponse = couponApplyService.apply(couponId, userId, couponApplyRequest);
        return ResponseEntity.ok(couponApplyResponse);
    }

    // WHAT: 쿠폰 사용 취소
    @PatchMapping("/{couponId}/apply/rollback")
    public ResponseEntity<Void> rollbackApplyCoupon(@PathVariable Long couponId, @Login Long userId) {
        couponApplyService.rollbackApply(couponId, userId);
        return ResponseEntity.ok().build();
    }
}