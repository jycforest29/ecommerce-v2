package ecommerce.platform.common.event.review;

import lombok.Builder;

public class ReviewCreatedEvent extends ReviewEvent {
    public static final String TOPIC = "review.events.created";

    @Builder
    public ReviewCreatedEvent(Long productId, int averageScore) {
        super(productId, averageScore);
    }

    @Override
    public String getTopic() {
        return TOPIC;
    }
}
