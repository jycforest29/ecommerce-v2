package ecommerce.platform.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LogoutTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public void add(String expiredToken) {
        redisTemplate.opsForValue().set(expiredToken, "logout", 6*1000*1000);
    }

    public boolean isExist(String token) {
        return redisTemplate.opsForValue().get(token) != null;
    }
}
