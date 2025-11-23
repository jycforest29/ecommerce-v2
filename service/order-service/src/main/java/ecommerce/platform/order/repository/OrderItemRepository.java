package ecommerce.platform.order.repository;

import ecommerce.platform.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long id);

    List<OrderItem> findByOrderIdIn(List<Long> orderIds);
}
