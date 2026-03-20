package ecommerce.platform.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class LogoutTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public void add(String expiredToken) {
        redisTemplate.opsForValue().set(expiredToken, "logout", 3600, TimeUnit.SECONDS);
    }

    public boolean isExist(String token) {
        return redisTemplate.opsForValue().get(token) != null;
    }
}
