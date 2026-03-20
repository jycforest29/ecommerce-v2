package ecommerce.platform.coupon.repository;

import ecommerce.platform.coupon.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
}