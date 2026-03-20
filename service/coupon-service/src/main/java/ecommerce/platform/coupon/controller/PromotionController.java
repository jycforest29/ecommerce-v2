package ecommerce.platform.coupon.controller;

import ecommerce.platform.common.annotations.Login;
import ecommerce.platform.coupon.dto.PromotionQueryResponse;
import ecommerce.platform.coupon.dto.PromotionRegisterRequest;
import ecommerce.platform.coupon.dto.PromotionRegisterResponse;
import ecommerce.platform.coupon.service.PromotionManageService;
import ecommerce.platform.coupon.service.PromotionQueryService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@RestController
public class PromotionController {

    private final PromotionManageService promotionManageService;
    private final PromotionQueryService promotionQueryService;

    @PostMapping
    public ResponseEntity<PromotionRegisterResponse> registerPromotion(@Login Long userId, @Valid @RequestBody PromotionRegisterRequest promotionRegisterRequest) {
        PromotionRegisterResponse promotionRegisterResponse = promotionManageService.register(promotionRegisterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionRegisterResponse);
    }

    @GetMapping
    public ResponseEntity<List<PromotionQueryResponse>> getAllPromotions() {
        List<PromotionQueryResponse> promotionQueryResponses = promotionQueryService.getAllPromotions();
        return ResponseEntity.ok(promotionQueryResponses);
    }

    @GetMapping("/{promotionId}")
    public ResponseEntity<PromotionQueryResponse> getPromotion(@PathVariable Long promotionId) {
        PromotionQueryResponse promotionQueryResponse = promotionQueryService.getPromotion(promotionId);
        return ResponseEntity.ok(promotionQueryResponse);
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePromotion(@Login Long userId, @RequestParam("promotionId") Long promotionId) {
        promotionManageService.remove(promotionId);
        return ResponseEntity.ok().build();
    }
}
