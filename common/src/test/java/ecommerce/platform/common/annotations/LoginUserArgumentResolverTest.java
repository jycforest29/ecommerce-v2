package ecommerce.platform.common.annotations;

import ecommerce.platform.common.exception.UnauthorizedAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginUserArgumentResolverTest {

    private final LoginUserArgumentResolver resolver = new LoginUserArgumentResolver();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증된 사용자의 userId를 Long으로 반환한다")
    void resolveArgument() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("42", null, List.of())
        );

        Object result = resolver.resolveArgument(null, null, null, null);

        assertThat(result).isEqualTo(42L);
    }

    @Test
    @DisplayName("인증 정보가 없으면 UnauthorizedAccessException 발생")
    void noAuthentication() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> resolver.resolveArgument(null, null, null, null))
                .isInstanceOf(UnauthorizedAccessException.class);
    }
}
