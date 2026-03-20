package ecommerce.platform.common.exception;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ErrorResponse(
        int status,
        String message,
        Instant timestamp
) {
    public static ErrorResponse of(int status, String message) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}