package ecommerce.platform.common.exception;

public class InputNotValidException extends RuntimeException {

    public InputNotValidException() {
        super("입력값이 유효하지 않습니다.");
    }

    public InputNotValidException(String message) {
        super(message);
    }
}
