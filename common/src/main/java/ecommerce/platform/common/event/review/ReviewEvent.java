package ecommerce.platform.common.event.review;

import ecommerce.platform.common.event.Event;
import lombok.Getter;

@Getter
public abstract class ReviewEvent extends Event {
    private Long productId;
    private int averageScore;

    protected ReviewEvent(Long productId, int averageScore) {
        super();
        this.productId = productId;
        this.averageScore = averageScore;
    }

    protected ReviewEvent() {
        super();
    }
}
