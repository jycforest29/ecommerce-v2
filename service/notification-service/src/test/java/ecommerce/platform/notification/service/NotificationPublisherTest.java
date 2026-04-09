package ecommerce.platform.notification.service;

import ecommerce.platform.notification.util.BucketUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherTest {

    @InjectMocks
    private NotificationPublisher notificationPublisher;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("userId에 해당하는 bucket 채널로 메시지를 발행한다")
    void publishToCorrectChannel() {
        notificationPublisher.publish(42L, "배송 시작");

        String expectedChannel = BucketUtil.channelForUser(42L);
        verify(redisTemplate).convertAndSend(expectedChannel, "42:배송 시작");
    }

    @Test
    @DisplayName("메시지 형식은 userId:payload이다")
    void messageFormat() {
        notificationPublisher.publish(1L, "쿠폰 만료 예정");

        String expectedChannel = BucketUtil.channelForUser(1L);
        verify(redisTemplate).convertAndSend(expectedChannel, "1:쿠폰 만료 예정");
    }
}
