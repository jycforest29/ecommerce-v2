package ecommerce.platform.coupon.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.coupon.CouponApplyRequestEvent;
import ecommerce.platform.common.event.coupon.CouponRollbackApplyEvent;
import ecommerce.platform.common.event.user.UserJoinEvent;
import ecommerce.platform.common.event.user.UserWithdrawEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final CouponIssueService couponIssueService;
    private final CouponApplyService couponApplyService;

    @KafkaListener(topics = {UserJoinEvent.TOPIC, UserWithdrawEvent.TOPIC})
    public void handleUserEvent(Event event, Acknowledgment ack) {
        if (event instanceof UserJoinEvent userJoinEvent) {
            couponIssueService.issueWelcomeCoupon(userJoinEvent.getUserId());
        } else if (event instanceof UserWithdrawEvent userWithdrawEvent) {
            couponIssueService.deleteAllCouponsByUser(userWithdrawEvent.getUserId());
        }
        ack.acknowledge();
    }

    @KafkaListener(topics = {CouponApplyRequestEvent.TOPIC, CouponRollbackApplyEvent.TOPIC})
    public void handleCouponEvent(Event event, Acknowledgment ack) {
        if (event instanceof CouponApplyRequestEvent couponApplyRequestEvent) {
            couponApplyService.applyFromOrder(couponApplyRequestEvent);
        } else if (event instanceof CouponRollbackApplyEvent couponRollbackApplyEvent) {
            couponApplyService.rollbackApplyFromOrder(couponRollbackApplyEvent);
        }
        ack.acknowledge();
    }
}