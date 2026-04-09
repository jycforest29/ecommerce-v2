package ecommerce.platform.order.repository;

import ecommerce.platform.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserId(Long userId);

    Page<Order> findAllByUserId(Long userId, Pageable pageable);
}
