package ecommerce.platform.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LogoutTokenRepositoryTest {

    private LogoutTokenRepository logoutTokenRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        logoutTokenRepository = new LogoutTokenRepository(redisTemplate);
    }

    @Test
    @DisplayName("토큰을 블랙리스트에 추가한다")
    void add() {
        logoutTokenRepository.add("expired-token");

        then(valueOperations).should().set("expired-token", "logout", 3600, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("블랙리스트에 존재하는 토큰이면 true를 반환한다")
    void isExistTrue() {
        given(valueOperations.get("expired-token")).willReturn("logout");

        assertThat(logoutTokenRepository.isExist("expired-token")).isTrue();
    }

    @Test
    @DisplayName("블랙리스트에 없는 토큰이면 false를 반환한다")
    void isExistFalse() {
        given(valueOperations.get("valid-token")).willReturn(null);

        assertThat(logoutTokenRepository.isExist("valid-token")).isFalse();
    }
}
