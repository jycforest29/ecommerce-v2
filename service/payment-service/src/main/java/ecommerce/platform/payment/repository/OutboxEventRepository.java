package ecommerce.platform.payment.repository;

import ecommerce.platform.common.event.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByIsPublishedFalse();

    List<OutboxEvent> findByIsPublishedFalseAndRetryCountLessThan(int maxRetryCount);
}
