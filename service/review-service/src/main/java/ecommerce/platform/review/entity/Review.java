package ecommerce.platform.review.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = true)
    private Long imageId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReviewScore score;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = true)
    private Instant modifiedAt;

    @Column(nullable = true)
    private Instant deletedAt;

    @Builder
    public Review(Long productId, Long userId, Long imageId, String title,
                  String content, ReviewScore score) {
        this.productId = productId;
        this.userId = userId;
        this.imageId = imageId;
        this.title = title;
        this.content = content;
        this.score = score;
        this.createdAt = Instant.now();
    }

    public void modify(Long imageId, String title, String content, ReviewScore score) {
        this.imageId = imageId;
        this.title = title;
        this.content = content;
        this.score = score;
        this.modifiedAt = Instant.now();
    }

    public boolean writtenBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void delete() {
        this.deletedAt = Instant.now();
    }
}