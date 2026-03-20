package ecommerce.platform.coupon.repository;

import ecommerce.platform.coupon.entity.Product;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.reviewCount = p.reviewCount + :reviewDelta WHERE p.id = :productId")
    void updateReviewCountByProductId(Long productId, int reviewDelta);
}
