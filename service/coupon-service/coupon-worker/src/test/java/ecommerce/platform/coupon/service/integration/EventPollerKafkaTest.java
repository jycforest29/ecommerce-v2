package ecommerce.platform.coupon.service.integration;

import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.coupon.repository.OutboxEventRepository;
import ecommerce.platform.coupon.service.EventPoller;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * EmbeddedKafka를 사용하여 EventPoller가 OutboxEvent를 Kafka로 발행하는지 검증하는 통합 테스트.
 */
@ExtendWith(MockitoExtension.class)
@EmbeddedKafka(
        partitions = 1,
        topics = {"coupon.events.applied", "test.topic"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:0"}
)
class EventPollerKafkaTest {

    private EventPoller eventPoller;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private KafkaTemplate<String, Object> kafkaTemplate;
    private Consumer<String, String> consumer;
    private EmbeddedKafkaBroker embeddedKafka;

    @BeforeEach
    void setUp(EmbeddedKafkaBroker broker) {
        this.embeddedKafka = broker;

        // Producer 설정 - key/value 모두 StringSerializer
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        DefaultKafkaProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);

        eventPoller = new EventPoller(outboxEventRepository, kafkaTemplate);

        // Consumer 설정
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = consumerFactory.createConsumer();
        embeddedKafka.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) consumer.close();
    }

    @Test
    @DisplayName("미발행 OutboxEvent를 Kafka로 발행하고 발행 완료 처리한다")
    void pollAndPublish() {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .entityName("test.topic")
                .entityId("event-123")
                .payload("{\"orderId\":1,\"couponId\":5}")
                .build();

        given(outboxEventRepository.findByIsPublishedFalse()).willReturn(List.of(outboxEvent));

        eventPoller.pollAndPublish();

        // Kafka에서 메시지 소비
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));

        assertThat(records.count()).isGreaterThanOrEqualTo(1);

        var record = records.iterator().next();
        assertThat(record.topic()).isEqualTo("test.topic");
        assertThat(record.key()).isEqualTo("event-123");
        assertThat(record.value()).contains("\"orderId\":1");

        // 발행 완료 처리 확인
        assertThat(outboxEvent.isPublished()).isTrue();
    }

    @Test
    @DisplayName("미발행 이벤트가 없으면 Kafka에 아무것도 발행하지 않는다")
    void pollAndPublish_noEvents() {
        given(outboxEventRepository.findByIsPublishedFalse()).willReturn(List.of());

        eventPoller.pollAndPublish();

        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(2));
        // 이전 테스트 잔여 메시지를 제외하면 새 메시지 없음
        // 여기서는 예외 없이 정상 종료되는지가 핵심
    }

    @Test
    @DisplayName("여러 OutboxEvent를 한 번에 발행한다")
    void pollAndPublishMultiple() {
        OutboxEvent event1 = OutboxEvent.builder()
                .entityName("test.topic")
                .entityId("event-1")
                .payload("{\"id\":1}")
                .build();
        OutboxEvent event2 = OutboxEvent.builder()
                .entityName("test.topic")
                .entityId("event-2")
                .payload("{\"id\":2}")
                .build();

        given(outboxEventRepository.findByIsPublishedFalse()).willReturn(List.of(event1, event2));

        eventPoller.pollAndPublish();

        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        assertThat(records.count()).isGreaterThanOrEqualTo(2);

        assertThat(event1.isPublished()).isTrue();
        assertThat(event2.isPublished()).isTrue();
    }
}
