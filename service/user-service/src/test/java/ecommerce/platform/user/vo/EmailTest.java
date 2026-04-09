package ecommerce.platform.user.vo;

import ecommerce.platform.common.exception.InputNotValidException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    @DisplayName("올바른 이메일 형식이면 생성에 성공한다")
    void validEmail() {
        Email email = new Email("test@example.com");
        assertThat(email.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("@가 없으면 예외가 발생한다")
    void missingAt() {
        assertThatThrownBy(() -> new Email("testexample.com"))
                .isInstanceOf(InputNotValidException.class);
    }

    @Test
    @DisplayName("도메인이 없으면 예외가 발생한다")
    void missingDomain() {
        assertThatThrownBy(() -> new Email("test@"))
                .isInstanceOf(InputNotValidException.class);
    }

    @Test
    @DisplayName("null이면 예외가 발생한다")
    void nullValue() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(InputNotValidException.class);
    }
}