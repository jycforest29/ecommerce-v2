package ecommerce.platform.user.vo;

import ecommerce.platform.common.exception.InputNotValidException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberTest {

    @Test
    @DisplayName("010-XXXX-XXXX 형식이면 생성에 성공한다")
    void validPhoneNumber() {
        PhoneNumber phoneNumber = new PhoneNumber("010-1234-5678");
        assertThat(phoneNumber.phoneNumber()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("하이픈이 없으면 예외가 발생한다")
    void missingHyphen() {
        assertThatThrownBy(() -> new PhoneNumber("01012345678"))
                .isInstanceOf(InputNotValidException.class);
    }

    @Test
    @DisplayName("010으로 시작하지 않으면 예외가 발생한다")
    void wrongPrefix() {
        assertThatThrownBy(() -> new PhoneNumber("011-1234-5678"))
                .isInstanceOf(InputNotValidException.class);
    }

    @Test
    @DisplayName("null이면 예외가 발생한다")
    void nullValue() {
        assertThatThrownBy(() -> new PhoneNumber(null))
                .isInstanceOf(InputNotValidException.class);
    }
}