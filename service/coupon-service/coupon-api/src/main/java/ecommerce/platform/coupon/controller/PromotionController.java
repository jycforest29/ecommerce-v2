package ecommerce.platform.coupon.controller;

import ecommerce.platform.common.annotations.Login;
import ecommerce.platform.coupon.dto.PromotionQueryResponse;
import ecommerce.platform.coupon.dto.PromotionRegisterRequest;
import ecommerce.platform.coupon.dto.PromotionRegisterResponse;
import ecommerce.platform.coupon.service.PromotionManageService;
import ecommerce.platform.coupon.service.PromotionQueryService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@RestController
public class PromotionController {

    private final PromotionManageService promotionManageService;
    private final PromotionQueryService promotionQueryService;

    // WHAT: 프로모션 등록
    @PostMapping
    public ResponseEntity<PromotionRegisterResponse> registerPromotion(@Login Long userId, @Valid @RequestBody PromotionRegisterRequest promotionRegisterRequest) {
        PromotionRegisterResponse promotionRegisterResponse = promotionManageService.register(userId, promotionRegisterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionRegisterResponse);
    }

    // WHAT: 등록된 모든 프로모션 조회
    @GetMapping
    public ResponseEntity<Page<PromotionQueryResponse>> getAllPromotions(Pageable pageable) {
        Page<PromotionQueryResponse> promotionQueryResponses = promotionQueryService.getAllPromotions(pageable);
        return ResponseEntity.ok(promotionQueryResponses);
    }

    // WHAT: 등록된 상세 프로모션 조회
    @GetMapping("/{promotionId}")
    public ResponseEntity<PromotionQueryResponse> getPromotion(@PathVariable Long promotionId) {
        PromotionQueryResponse promotionQueryResponse = promotionQueryService.getPromotion(promotionId);
        return ResponseEntity.ok(promotionQueryResponse);
    }

    // WHAT: 프로모션 제거
    @DeleteMapping
    public ResponseEntity<Void> deletePromotion(@Login Long userId, @RequestParam("promotionId") Long promotionId) {
        promotionManageService.remove(userId, promotionId);
        return ResponseEntity.ok().build();
    }
}
