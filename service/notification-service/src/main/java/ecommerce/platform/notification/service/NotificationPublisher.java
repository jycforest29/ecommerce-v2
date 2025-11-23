package ecommerce.platform.notification.service;

import ecommerce.platform.notification.util.BucketUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final StringRedisTemplate redisTemplate;

    public void publish(Long userId, String payload) {
        String channel = BucketUtil.channelForUser(userId);
        String message = userId + ":" + payload;
        redisTemplate.convertAndSend(channel, message);
    }
}