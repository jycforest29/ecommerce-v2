package ecommerce.platform.order.controller;

import ecommerce.platform.common.annotations.Login;
import ecommerce.platform.order.dto.OrderCreateRequest;
import ecommerce.platform.order.dto.OrderCreateResponse;
import ecommerce.platform.order.dto.OrderQueryResponse;
import ecommerce.platform.order.service.OrderManageService;
import ecommerce.platform.order.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderManageService orderManageService;
    private final OrderQueryService orderQueryService;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(@Login Long userId, @Valid @RequestBody OrderCreateRequest orderCreateRequest)  {
        OrderCreateResponse orderCreateResponse = orderManageService.createOrder(userId, orderCreateRequest);
        return ResponseEntity.ok(orderCreateResponse);
    }

    @GetMapping
    public ResponseEntity<Page<OrderQueryResponse>> queryOrders(
            @Login Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<OrderQueryResponse> orderQueryResponses = orderQueryService.queryOrders(userId, pageable);
        return ResponseEntity.ok(orderQueryResponses);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderQueryResponse> queryOrder(@Login Long userId, @PathVariable Long orderId) {
        OrderQueryResponse orderQueryResponse = orderQueryService.queryOrder(userId, orderId);
        return ResponseEntity.ok(orderQueryResponse);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@Login Long userId, @PathVariable Long orderId) {
        orderManageService.cancelOrder(userId, orderId);
        return ResponseEntity.ok().build();
    }

}
