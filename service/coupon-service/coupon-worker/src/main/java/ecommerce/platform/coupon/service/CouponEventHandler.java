package ecommerce.platform.coupon.service;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.coupon.CouponApplyRequestEvent;
import ecommerce.platform.common.event.coupon.CouponRollbackApplyEvent;
import ecommerce.platform.common.event.user.UserJoinEvent;
import ecommerce.platform.common.event.user.UserWithdrawEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponEventHandler {

    private final CouponIssueService couponIssueService;
    private final CouponApplyService couponApplyService;

    public void handle(Event event) {
        switch (event) {
            case UserJoinEvent e -> couponIssueService.issueWelcomeCoupon(e.getUserId());
            case UserWithdrawEvent e -> couponIssueService.deleteAllCouponsByUser(e.getUserId());
            case CouponApplyRequestEvent e -> couponApplyService.applyFromOrder(e);
            case CouponRollbackApplyEvent e -> couponApplyService.rollbackApplyFromOrder(e);
            default -> throw new IllegalArgumentException("지원하지 않는 이벤트 타입: " + event.getClass().getSimpleName());
        }
    }
}