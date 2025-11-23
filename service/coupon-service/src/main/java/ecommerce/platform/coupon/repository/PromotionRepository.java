package ecommerce.platform.coupon.repository;

import ecommerce.platform.coupon.entity.Promotion;
import ecommerce.platform.coupon.service.CouponIssueService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByName(String promotionName);
    Optional<Promotion> getAndDeleteById(Long promotionId);
}
