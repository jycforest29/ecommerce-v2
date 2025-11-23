package ecommerce.platform.order.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long productOptionId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int priceSnapshot;

    @Column(nullable = true)
    private Long couponId = null;

    @Column(nullable = true)
    private Integer discountRate;

    @Column(nullable = true)
    private Integer discountAmount;

    @Builder
    public OrderItem(Order order, Long productId, Long productOptionId, int quantity, int priceSnapshot) {
        this.order = order;
        this.productId = productId;
        this.productOptionId = productOptionId;
        this.quantity = quantity;
        this.priceSnapshot = priceSnapshot;
    }

    public void applyCoupon(Long couponId, int discountRate, int discountAmount) {
        this.couponId = couponId;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
    }

}
