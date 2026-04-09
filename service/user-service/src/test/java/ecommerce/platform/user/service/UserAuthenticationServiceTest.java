package ecommerce.platform.user.service;

import ecommerce.platform.user.dto.MemberJoinRequest;
import ecommerce.platform.user.dto.MemberLoginRequest;
import ecommerce.platform.user.exception.MemberAlreadyExistsException;
import ecommerce.platform.user.repository.LogoutTokenRepository;
import ecommerce.platform.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationServiceTest {

    @InjectMocks
    private UserAuthenticationService userAuthenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LogoutTokenRepository logoutTokenRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private PasswordManager passwordManager;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Authentication createAuthentication(String username, String token) {
        User principal = new User(username, "", Collections.emptyList());
        return new UsernamePasswordAuthenticationToken(principal, token, Collections.emptyList());
    }

    @Nested
    @DisplayName("회원가입 - join")
    class Join {

        @Test
        @DisplayName("신규 사용자가 가입하면 토큰을 반환한다")
        void joinSuccess() {
            MemberJoinRequest request = new MemberJoinRequest(
                    "user01", "Abcde1@x", "test@example.com", "010-1234-5678"
            );
            given(userRepository.existsByUserName("user01")).willReturn(false);
            given(passwordManager.encrypt("Abcde1@x")).willReturn("encrypted");

            Authentication auth = createAuthentication("user01", "jwt-token");
            given(authenticationManager.authenticate(any())).willReturn(auth);
            given(tokenManager.issueToken(auth)).willReturn("jwt-token");

            String token = userAuthenticationService.join(request);

            assertThat(token).isEqualTo("jwt-token");
            then(userRepository).should().saveAndFlush(any());
        }

        @Test
        @DisplayName("이미 존재하는 사용자이면 예외가 발생한다")
        void joinDuplicate() {
            MemberJoinRequest request = new MemberJoinRequest(
                    "user01", "Abcde1@x", "test@example.com", "010-1234-5678"
            );
            given(userRepository.existsByUserName("user01")).willReturn(true);

            assertThatThrownBy(() -> userAuthenticationService.join(request))
                    .isInstanceOf(MemberAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("로그인 - login")
    class Login {

        @Test
        @DisplayName("올바른 자격증명으로 로그인하면 토큰을 반환한다")
        void loginSuccess() {
            MemberLoginRequest request = new MemberLoginRequest("user01", "Abcde1@x");
            Authentication auth = createAuthentication("user01", "jwt-token");
            given(authenticationManager.authenticate(any())).willReturn(auth);
            given(tokenManager.issueToken(auth)).willReturn("jwt-token");

            String token = userAuthenticationService.login(request);

            assertThat(token).isEqualTo("jwt-token");
        }
    }

    @Nested
    @DisplayName("로그아웃 - logout")
    class Logout {

        @Test
        @DisplayName("로그아웃하면 토큰이 블랙리스트에 추가된다")
        void logoutSuccess() {
            Authentication auth = createAuthentication("user01", "jwt-token");
            SecurityContextHolder.getContext().setAuthentication(auth);

            userAuthenticationService.logout();

            then(logoutTokenRepository).should().add("jwt-token");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("회원탈퇴 - withdraw")
    class Withdraw {

        @Test
        @DisplayName("탈퇴하면 사용자를 삭제하고 로그아웃 처리한다")
        void withdrawSuccess() {
            Authentication auth = createAuthentication("user01", "jwt-token");
            SecurityContextHolder.getContext().setAuthentication(auth);

            userAuthenticationService.withdraw();

            then(userRepository).should().deleteByUserName("user01");
            then(logoutTokenRepository).should().add("jwt-token");
        }
    }
}
