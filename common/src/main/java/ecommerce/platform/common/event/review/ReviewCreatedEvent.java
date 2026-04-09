package ecommerce.platform.common.event.review;

import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewCreatedEvent extends Event {
    public static final String TOPIC = "review.events.created";

    private Long productId;
    private int averageScore;

    @Builder
    public ReviewCreatedEvent(Long productId, int averageScore) {
        super();
        this.productId = productId;
        this.averageScore = averageScore;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
