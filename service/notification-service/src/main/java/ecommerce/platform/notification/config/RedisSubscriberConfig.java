package ecommerce.platform.notification.config;

import ecommerce.platform.notification.service.NotificationSubscriber;
import ecommerce.platform.notification.util.BucketUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisSubscriberConfig {
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            NotificationSubscriber notificationSubscriber
    ) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);

        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(notificationSubscriber, "onMessage");
        for (int i = 0; i < BucketUtil.BUCKETS; i++) {
            redisMessageListenerContainer.addMessageListener(messageListenerAdapter, new ChannelTopic(BucketUtil.channel(i)));
        }

        return redisMessageListenerContainer;
    }
}
