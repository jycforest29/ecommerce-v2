package ecommerce.platform.common.event.review;

import lombok.Builder;

public class ReviewDeletedEvent extends ReviewEvent {
    public static final String TOPIC = "review.events.deleted";

    protected ReviewDeletedEvent() {}

    @Builder
    public ReviewDeletedEvent(Long productId, int averageScore) {
        super(productId, averageScore);
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
