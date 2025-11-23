package ecommerce.platform.user.vo;

import ecommerce.platform.common.exception.InputNotValidException;

public final class RegexValidator {

    private RegexValidator() {}

    static void validate(String target, String regex) {
        if (target == null || !target.matches(regex)) {
            throw new InputNotValidException("입력값이 형식에 맞지 않습니다: " + regex);
        }
    }
}
