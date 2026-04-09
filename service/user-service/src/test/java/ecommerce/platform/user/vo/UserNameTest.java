package ecommerce.platform.user.vo;

import ecommerce.platform.common.exception.InputNotValidException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserNameTest {

    @Test
    @DisplayName("5~8자 영문/숫자 조합이면 생성에 성공한다")
    void validUserName() {
        UserName userName = new UserName("user01");
        assertThat(userName.userName()).isEqualTo("user01");
    }

    @Test
    @DisplayName("4자 이하이면 예외가 발생한다")
    void tooShort() {
        assertThatThrownBy(() -> new UserName("ab12"))
                .isInstanceOf(InputNotValidException.class);
    }

    @Test
    @DisplayName("9자 이상이면 예외가 발생한다")
    void tooLong() {
        assertThatThrownBy(() -> new UserName("abcde12345"))
                .isInstanceOf(InputNotValidException.class);
    }

    @Test
    @DisplayName("특수문자가 포함되면 예외가 발생한다")
    void specialCharacter() {
        assertThatThrownBy(() -> new UserName("user@1"))
                .isInstanceOf(InputNotValidException.class);
    }

    @Test
    @DisplayName("null이면 예외가 발생한다")
    void nullValue() {
        assertThatThrownBy(() -> new UserName(null))
                .isInstanceOf(InputNotValidException.class);
    }
}