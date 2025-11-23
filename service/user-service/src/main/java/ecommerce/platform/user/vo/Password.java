package ecommerce.platform.user.vo;

import ecommerce.platform.common.exception.InputNotValidException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Password(@Column(nullable = false) String password) {
    private static final String REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@#$!%])[A-Za-z0-9@#$!%]{8,12}$";

    public static void validateRawPassword(String rawPassword) {
        if (rawPassword == null || !rawPassword.matches(REGEX)) {
            throw new InputNotValidException("비밀번호는 8~12자, 대/소문자, 숫자, 특수문자를 포함해야 합니다.");
        }
    }
}
