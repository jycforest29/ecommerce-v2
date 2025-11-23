package ecommerce.platform.order.entity;

import ecommerce.platform.common.constants.PaymentMethod;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int totalPriceSnapshot;

    @Column(nullable = true)
    private Long couponId;

    @Column(nullable = true)
    private Integer discountRate;

    @Column(nullable = true)
    private Integer discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = true)
    private Long paymentId;

    @Column(nullable = true)
    private Instant paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private PaymentMethod paymentMethod;

    @Builder
    public Order(Long userId, int totalQuantity, int totalPriceSnapshot) {
        this.userId = userId;
        this.createdAt = Instant.now();
        this.totalQuantity = totalQuantity;
        this.totalPriceSnapshot = totalPriceSnapshot;
        this.orderStatus = OrderStatus.CREATED;
    }

    public void applyCoupon(Long couponId, int discountRate, int discountAmount) {
        this.couponId = couponId;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.orderStatus = OrderStatus.COUPON_APPLIED;
    }

    public void markStockDeducted() {
        this.orderStatus = OrderStatus.STOCK_DEDUCTED;
    }

    public void markPaymentPending() {
        this.orderStatus = OrderStatus.PAYMENT_PENDING;
    }

    public void completePayment(Long paymentId, PaymentMethod paymentMethod) {
        this.paymentId = paymentId;
        this.paidAt = Instant.now();
        this.orderStatus = OrderStatus.PAID;
        this.paymentMethod = paymentMethod;
    }
}
