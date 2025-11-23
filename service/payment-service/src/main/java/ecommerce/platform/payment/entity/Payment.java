package ecommerce.platform.payment.entity;

import ecommerce.platform.common.constants.PaymentMethod;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int finalPrice;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = true)
    private Instant cancelledAt;

    @Column(nullable = true)
    private Instant refundedAt;

    @Builder
    public Payment(Long orderId, Long userId, PaymentMethod paymentMethod, int totalPrice, int discountAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
        this.discountAmount = discountAmount;
        this.finalPrice = totalPrice - discountAmount;
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.createdAt = Instant.now();
    }

    public void cancel() {
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    public void refund() {
        this.paymentStatus = PaymentStatus.REFUNDED;
        this.refundedAt = Instant.now();
    }
}