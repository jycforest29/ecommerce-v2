package ecommerce.platform.user.exception;

public class MemberAlreadyExistsException extends RuntimeException {

    public MemberAlreadyExistsException() {
        super("이미 존재하는 사용자입니다.");
    }
}