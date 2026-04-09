package ecommerce.platform.common.event.review;

import ecommerce.platform.common.event.Event;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewDeletedEvent extends Event {
    public static final String TOPIC = "review.events.deleted";

    private Long productId;
    private int averageScore;

    @Builder
    public ReviewDeletedEvent(Long productId, int averageScore) {
        super();
        this.productId = productId;
        this.averageScore = averageScore;
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
