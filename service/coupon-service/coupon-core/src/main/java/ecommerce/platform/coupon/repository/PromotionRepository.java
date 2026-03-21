package ecommerce.platform.coupon.repository;

import ecommerce.platform.coupon.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByPromotionName(String promotionName);
}
