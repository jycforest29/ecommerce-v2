package ecommerce.platform.payment.controller;

import ecommerce.platform.common.annotations.Login;
import ecommerce.platform.payment.dto.PaymentCreateResponse;
import ecommerce.platform.payment.dto.PaymentCreateRequest;
import ecommerce.platform.payment.dto.PaymentQueryResponse;
import ecommerce.platform.payment.service.PaymentManageService;
import ecommerce.platform.payment.service.PaymentQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@RestController
public class PaymentController {

    private final PaymentManageService paymentManageService;
    private final PaymentQueryService paymentQueryService;

    @PostMapping("/{orderId}")
    public ResponseEntity<PaymentCreateResponse> createPayment(
            @PathVariable Long orderId,
            @Login Long userId,
            @Valid @RequestBody PaymentCreateRequest paymentRequest) {
        PaymentCreateResponse response = paymentManageService.createPayment(orderId, paymentRequest, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PaymentQueryResponse>> getPayments(@Login Long userId) {
        List<PaymentQueryResponse> responses = paymentQueryService.queryPayments(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentQueryResponse> getPayment(
            @PathVariable Long paymentId,
            @Login Long userId) {
        PaymentQueryResponse response = paymentQueryService.queryPayment(userId, paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentQueryResponse> getPaymentByOrderId(
            @PathVariable Long orderId,
            @Login Long userId) {
        PaymentQueryResponse response = paymentQueryService.queryPaymentByOrderId(userId, orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentQueryResponse> refundPayment(
            @PathVariable Long paymentId,
            @Login Long userId) {
        PaymentQueryResponse response = paymentManageService.refund(paymentId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<PaymentQueryResponse> cancelPayment(
            @PathVariable Long orderId,
            @Login Long userId) {
        PaymentQueryResponse response = paymentManageService.cancel(orderId, userId);
        return ResponseEntity.ok(response);
    }
}