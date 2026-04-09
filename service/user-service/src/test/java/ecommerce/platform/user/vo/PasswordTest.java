package ecommerce.platform.user.vo;

import ecommerce.platform.common.exception.InputNotValidException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordTest {

    @Test
    @DisplayName("대/소문자, 숫자, 특수문자 포함 8~12자이면 검증에 성공한다")
    void validPassword() {
        assertThatCode(() -> Password.validateRawPassword("Abcde1@x"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("7자 이하이면 예외가 발생한다")
    void tooShort() {
        assertThatThrownBy(() -> Password.validateRawPassword("Abc1@x"))
                .isInstanceOf(InputNotValidException.class);
    }

    @Test
    @DisplayName("13자 이상이면 예외가 발생한다")
    void tooLong() {
        assertThatThrownBy(() -> Password.validateRawPassword("Abcde1@xyzzzz"))
                .isInstanceOf(InputNotValidException.class);
    }

    @Test
    @DisplayName("대문자가 없으면 예외가 발생한다")
    void missingUpperCase() {
        assertThatThrownBy(() -> Password.validateRawPassword("abcde1@x"))
                .isInstanceOf(InputNotValidException.class);
    }

    @Test
    @DisplayName("소문자가 없으면 예외가 발생한다")
    void missingLowerCase() {
        assertThatThrownBy(() -> Password.validateRawPassword("ABCDE1@X"))
                .isInstanceOf(InputNotValidException.class);
    }

    @Test
    @DisplayName("숫자가 없으면 예외가 발생한다")
    void missingDigit() {
        assertThatThrownBy(() -> Password.validateRawPassword("Abcdefg@"))
                .isInstanceOf(InputNotValidException.class);
    }

    @Test
    @DisplayName("특수문자가 없으면 예외가 발생한다")
    void missingSpecialChar() {
        assertThatThrownBy(() -> Password.validateRawPassword("Abcdefg1"))
                .isInstanceOf(InputNotValidException.class);
    }

    @Test
    @DisplayName("null이면 예외가 발생한다")
    void nullValue() {
        assertThatThrownBy(() -> Password.validateRawPassword(null))
                .isInstanceOf(InputNotValidException.class);
    }
}