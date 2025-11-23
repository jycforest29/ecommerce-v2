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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        ProductUpdateResponse productUpdateResponse = productCommandService.modifyProduct(productId, productUpdateRequest);
        return ResponseEntity.ok(productUpdateResponse);
    }

    @GetMapping
    public ResponseEntity<List<ProductQueryResponse>> queryProducts() {
        List<ProductQueryResponse> responses = productQueryService.queryProducts();
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
        productCommandService.deleteProduct(productId, userId);
        return ResponseEntity.ok().build();
    }
}