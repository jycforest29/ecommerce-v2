package ecommerce.platform.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = true)
    private Instant readAt;

    @Column(nullable = true)
    private Instant expiredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private NotificationType notificationType;

    @Column(nullable = false)
    private String dedupeKey;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 300)
    private String body;

    @Builder
    private Notification(Long userId, NotificationType notificationType, String dedupeKey, String title, String body, Instant expiredAt) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.dedupeKey = dedupeKey;
        this.title = title;
        this.body = body;
        this.expiredAt = expiredAt;
        this.createdAt = Instant.now();
    }

    public boolean isRead() {
        return readAt != null;
    }

    public void markAsRead() {
        if (isRead()) return;
        this.readAt = Instant.now();
    }
}