package ecommerce.platform.coupon.controller;

import ecommerce.platform.common.annotations.Login;
import ecommerce.platform.coupon.dto.ProductCreateRequest;
import ecommerce.platform.coupon.dto.ProductCreateResponse;
import ecommerce.platform.coupon.dto.ProductQueryResponse;
import ecommerce.platform.coupon.dto.ProductUpdateRequest;
import ecommerce.platform.coupon.dto.ProductUpdateResponse;
import ecommerce.platform.coupon.service.ProductManageService;
import ecommerce.platform.coupon.service.ProductQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@RestController
public class ProductController {

    private final ProductManageService productManageService;
    private final ProductQueryService productQueryService;

    @PostMapping
    public ResponseEntity<ProductCreateResponse> createProduct(
            @Login Long userId,
            @Valid @RequestBody ProductCreateRequest productCreateRequest) {
        ProductCreateResponse productCreateResponse = productManageService.createProduct(userId, productCreateRequest);
        return ResponseEntity.ok(productCreateResponse);
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ProductUpdateResponse> modifyProduct(
            @Login Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest productUpdateRequest) {
        ProductUpdateResponse productUpdateResponse = productManageService.modifyProduct(productId, userId, productUpdateRequest);
        return ResponseEntity.ok(productUpdateResponse);
    }

    @GetMapping
    public ResponseEntity<Page<ProductQueryResponse>> queryProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<ProductQueryResponse> responses = productQueryService.queryProducts(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductQueryResponse> queryProduct(@PathVariable Long productId) {
        ProductQueryResponse productQueryResponse = productQueryService.queryProduct(productId);
        return ResponseEntity.ok(productQueryResponse);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @Login Long userId,
            @PathVariable Long productId) {
        productManageService.deleteProduct(productId, userId);
        return ResponseEntity.ok().build();
    }
}