package ecommerce.platform.common.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public abstract class Event implements Serializable {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    protected String eventId;

    protected Event() {
        this.eventId = UUID.randomUUID().toString();
    }

    public abstract String getTopic();

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize " + getClass().getSimpleName(), e);
        }
    }
}
