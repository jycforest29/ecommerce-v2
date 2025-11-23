package ecommerce.platform.common.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException() {
        super("엔티티를 찾을 수 없습니다.");
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
