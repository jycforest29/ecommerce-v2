package ecommerce.platform.coupon.repository;

import ecommerce.platform.coupon.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    @Modifying
    @Query("UPDATE ProductOption po SET po.stock = po.stock - :quantity, " +
            "po.available = CASE WHEN po.stock - :quantity > 0 THEN true ELSE false END " +
            "WHERE po.id = :id AND po.stock >= :quantity")
    int deductStockConditionally(@Param("id") Long id, @Param("quantity") int quantity);
}