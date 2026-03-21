package ecommerce.platform.coupon.service;

import ecommerce.platform.common.event.coupon.CouponAppliedEvent;
import ecommerce.platform.common.event.coupon.CouponApplyRequestEvent;
import ecommerce.platform.common.event.coupon.CouponRollbackApplyEvent;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.common.util.OutboxEventGenerator;
import ecommerce.platform.coupon.dto.CouponApplyRequest;
import ecommerce.platform.coupon.dto.CouponApplyResponse;
import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.entity.CouponTargetItem;
import ecommerce.platform.coupon.exception.CouponFailedToApplyException;
import ecommerce.platform.coupon.exception.CouponFailedToRollbackApplyException;
import ecommerce.platform.coupon.repository.CouponLogRepository;
import ecommerce.platform.coupon.repository.CouponRepository;
import ecommerce.platform.coupon.repository.OutboxEventRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CouponApplyService {

    private final CouponRepository couponRepository;
    private final CouponLogRepository couponLogRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public CouponApplyResponse apply(Long couponId, Long userId, CouponApplyRequest couponApplyRequest) {
        final var coupon = EntityFinder.findEntity(couponRepository, couponId);
        final var orderItems = couponApplyRequest.orderItems();

        if (!Objects.equals(coupon.getUserId(), userId) || !coupon.isAbleToApply(orderItems)) {
            throw new CouponFailedToApplyException();
        }

        applyInternal(coupon);

        int totalPrice = orderItems.stream()
                .mapToInt(orderItem -> orderItem.getQuantity() * orderItem.getPrice())
                .sum();
        int discountAmount = coupon.calculateDiscountAmount(orderItems);

        return CouponApplyResponse.from(Instant.now(), totalPrice, discountAmount);
    }

    @Transactional
    public void rollbackApply(Long couponId, Long userId) {
        final var coupon = EntityFinder.findEntity(couponRepository, couponId);

        if (!Objects.equals(coupon.getUserId(), userId) || !coupon.isAbleToRollbackApply()) {
            throw new CouponFailedToRollbackApplyException();
        }

        rollbackInternal(coupon);
    }

    @Transactional
    public void applyFromOrder(CouponApplyRequestEvent event) {
        List<Coupon> coupons = couponRepository.findAllByUserId(event.getUserId());

        for (Coupon coupon : coupons) {
            List<CouponTargetItem> targetItems = event.getOrderItemInfos().stream()
                    .map(info -> CouponTargetItem.builder()
                            .price(info.getPrice())
                            .quantity(info.getQuantity())
                            .build())
                    .toList();

            if (coupon.isAbleToApply(targetItems)) {
                applyInternal(coupon);
                int discountAmount = coupon.calculateDiscountAmount(targetItems);

                CouponAppliedEvent appliedEvent = CouponAppliedEvent.builder()
                        .orderId(event.getOrderId())
                        .couponId(coupon.getCouponId())
                        .discountRate(coupon.getDiscountRate())
                        .discountAmount(discountAmount)
                        .build();
                outboxEventRepository.save(OutboxEventGenerator.publish(appliedEvent));
                return;
            }
        }
    }

    @Transactional
    public void rollbackApplyFromOrder(CouponRollbackApplyEvent event) {
        // saga 보상 트랜잭션: 주문 취소 시 쿠폰 적용 해제
    }

    private void applyInternal(Coupon coupon) {
        int updatedRows = couponRepository.applyConditionally(coupon.getCouponId());
        if (updatedRows == 0) {
            throw new CouponFailedToApplyException();
        }
        var couponLog = CouponLogFactory.apply(coupon);
        couponLogRepository.save(couponLog);
    }

    private void rollbackInternal(Coupon coupon) {
        int updatedRows = couponRepository.rollbackApplyConditionally(coupon.getCouponId());
        if (updatedRows == 0) {
            throw new CouponFailedToRollbackApplyException();
        }
        var couponLog = CouponLogFactory.applyCancel(coupon);
        couponLogRepository.save(couponLog);
    }
}