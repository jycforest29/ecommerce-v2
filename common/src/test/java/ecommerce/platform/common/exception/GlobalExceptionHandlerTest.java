package ecommerce.platform.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("EntityNotFoundException → 404 NOT_FOUND")
    void entityNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleEntityNotFoundException(new EntityNotFoundException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    @DisplayName("UnauthorizedAccessException → 403 FORBIDDEN")
    void unauthorized() {
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorizedAccessException(new UnauthorizedAccessException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().status()).isEqualTo(403);
    }

    @Test
    @DisplayName("InputNotValidException → 400 BAD_REQUEST")
    void inputNotValid() {
        ResponseEntity<ErrorResponse> response = handler.handleInputNotValidException(new InputNotValidException("잘못된 입력"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("잘못된 입력");
    }

    @Test
    @DisplayName("IllegalStateException → 409 CONFLICT")
    void illegalState() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalStateException(new IllegalStateException("상태 충돌"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo("상태 충돌");
    }

    @Test
    @DisplayName("일반 Exception → 500 INTERNAL_SERVER_ERROR")
    void genericException() {
        ResponseEntity<ErrorResponse> response = handler.handleException(new RuntimeException("서버 오류"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("서버 내부 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("ErrorResponse에 timestamp가 포함된다")
    void errorResponseTimestamp() {
        ResponseEntity<ErrorResponse> response = handler.handleEntityNotFoundException(new EntityNotFoundException());

        assertThat(response.getBody().timestamp()).isNotNull();
    }
}