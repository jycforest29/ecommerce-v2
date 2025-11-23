package ecommerce.platform.common.util;

import ecommerce.platform.common.event.Event;
import ecommerce.platform.common.event.OutboxEvent;

public class OutboxEventGenerator {
    public static OutboxEvent publish(Event event) {
        return OutboxEvent.builder()
                .entityName(event.getTopic())
                .entityId(event.getEventId())
                .payload(event.toJson())
                .build();
    }
}
