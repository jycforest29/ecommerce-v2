package ecommerce.platform.coupon.entity;

import ecommerce.platform.common.util.JsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponLog {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long couponLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Coupon coupon;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant loggedAt;

    @Enumerated(value = EnumType.STRING)
    private CouponStatus couponStatus;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> metadata;

    @Builder
    private CouponLog(Coupon coupon, CouponStatus couponStatus, Long userId, Map<String, Object> metadata) {
        this.coupon = coupon;
        this.loggedAt = Instant.now();
        this.couponStatus = couponStatus;
        this.userId = userId;
        this.metadata = metadata;
    }
}
