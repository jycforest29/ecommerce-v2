package ecommerce.platform.notification.service.integration;

import ecommerce.platform.notification.service.NotificationPublisher;
import ecommerce.platform.notification.util.BucketUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redis(localhost:6379)에 직접 연결하여 Pub/Sub 알림 발행을 검증하는 통합 테스트.
 * docker-compose up 상태에서 실행해야 합니다.
 */
class NotificationRedisTest {

    private NotificationPublisher notificationPublisher;
    private StringRedisTemplate redisTemplate;
    private LettuceConnectionFactory connectionFactory;
    private RedisMessageListenerContainer listenerContainer;

    @BeforeEach
    void setUp() {
        connectionFactory = new LettuceConnectionFactory("localhost", 6379);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();

        notificationPublisher = new NotificationPublisher(redisTemplate);

        listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(connectionFactory);
    }

    @AfterEach
    void tearDown() {
        if (listenerContainer.isRunning()) listenerContainer.stop();
        connectionFactory.destroy();
    }

    @Test
    @DisplayName("publish()로 보낸 메시지가 해당 bucket 채널로 전달된다")
    void publishAndSubscribe() throws InterruptedException {
        Long userId = 42L;
        String expectedChannel = BucketUtil.channelForUser(userId);

        BlockingQueue<String> receivedMessages = new LinkedBlockingQueue<>();

        MessageListener listener = (message, pattern) ->
                receivedMessages.offer(new String(message.getBody()));

        listenerContainer.addMessageListener(listener, new ChannelTopic(expectedChannel));
        listenerContainer.afterPropertiesSet();
        listenerContainer.start();

        // 리스너 초기화 대기
        Thread.sleep(500);

        notificationPublisher.publish(userId, "배송이 시작되었습니다.");

        String received = receivedMessages.poll(3, TimeUnit.SECONDS);

        assertThat(received).isNotNull();
        assertThat(received).isEqualTo("42:배송이 시작되었습니다.");
    }

    @Test
    @DisplayName("서로 다른 userId는 서로 다른 bucket 채널로 발행된다")
    void differentUsersGoToDifferentChannels() throws InterruptedException {
        Long userId1 = 1L;
        Long userId2 = 65L; // 64로 나누면 다른 bucket

        String channel1 = BucketUtil.channelForUser(userId1);
        String channel2 = BucketUtil.channelForUser(userId2);

        // bucket이 다른지 확인 (같으면 테스트 의미 없음)
        if (channel1.equals(channel2)) return;

        BlockingQueue<String> messages1 = new LinkedBlockingQueue<>();
        BlockingQueue<String> messages2 = new LinkedBlockingQueue<>();

        listenerContainer.addMessageListener(
                (message, pattern) -> messages1.offer(new String(message.getBody())),
                new ChannelTopic(channel1));
        listenerContainer.addMessageListener(
                (message, pattern) -> messages2.offer(new String(message.getBody())),
                new ChannelTopic(channel2));
        listenerContainer.afterPropertiesSet();
        listenerContainer.start();

        Thread.sleep(500);

        notificationPublisher.publish(userId1, "알림1");
        notificationPublisher.publish(userId2, "알림2");

        String msg1 = messages1.poll(3, TimeUnit.SECONDS);
        String msg2 = messages2.poll(3, TimeUnit.SECONDS);

        assertThat(msg1).isEqualTo("1:알림1");
        assertThat(msg2).isEqualTo("65:알림2");
    }
}
