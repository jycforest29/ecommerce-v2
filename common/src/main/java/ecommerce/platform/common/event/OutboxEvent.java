package ecommerce.platform.common.event;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Entity
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityName;

    @Column(nullable = false)
    private String entityId;

    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private boolean isPublished;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private Instant createdAt;

    @Builder
    public OutboxEvent(String entityName, String entityId, String payload) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.payload = payload;
        this.isPublished = false;
        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    public void markPublished() {
        this.isPublished = true;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}
