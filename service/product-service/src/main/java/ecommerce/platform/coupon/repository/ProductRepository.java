package ecommerce.platform.coupon.repository;

import ecommerce.platform.coupon.entity.Product;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stockCount = p.stockCount + :stockDelta WHERE p.id = :productId")
    void updateStockCountByProductId(Long productId, int stockDelta);

    @Modifying
    @Transactional
    @Query("UPDATE s.reviewCount SET s.stockCount = s.stockCount + :reviewDelta WHERE s.productId = :productId")
    void updateReviewCountByProductId(Long productId, int reviewDelta);
}
