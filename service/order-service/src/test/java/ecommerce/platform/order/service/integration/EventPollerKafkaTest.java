package ecommerce.platform.order.service.integration;

import ecommerce.platform.common.event.OutboxEvent;
import ecommerce.platform.order.repository.OutboxEventRepository;
import ecommerce.platform.order.service.EventPoller;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * EmbeddedKafka로 Order EventPoller의 Outbox→Kafka 발행을 검증하는 통합 테스트.
 */
@ExtendWith(MockitoExtension.class)
@EmbeddedKafka(
        partitions = 1,
        topics = {"order.poll.single", "order.poll.multi"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:0"}
)
class EventPollerKafkaTest {

    private EventPoller eventPoller;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private KafkaTemplate<String, Object> kafkaTemplate;
    private EmbeddedKafkaBroker embeddedKafka;

    @BeforeEach
    void setUp(EmbeddedKafkaBroker broker) {
        this.embeddedKafka = broker;

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        DefaultKafkaProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);

        eventPoller = new EventPoller(outboxEventRepository, kafkaTemplate);
    }

    private Consumer<String, String> createConsumer(String topic) {
        Map<String, Object> props = KafkaTestUtils.consumerProps("order-test-" + topic, "true", embeddedKafka);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(props).createConsumer();
        consumer.subscribe(Collections.singletonList(topic));
        return consumer;
    }

    @Test
    @DisplayName("CouponApplyRequestEvent를 Kafka로 발행하고 발행 완료 처리한다")
    void pollAndPublishOutboxEvent() {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .entityName("order.poll.single")
                .entityId("order-event-1")
                .payload("{\"orderId\":1,\"userId\":1}")
                .build();

        given(outboxEventRepository.findByIsPublishedFalse()).willReturn(List.of(outboxEvent));

        eventPoller.pollAndPublish();

        try (Consumer<String, String> consumer = createConsumer("order.poll.single")) {
            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
            List<ConsumerRecord<String, String>> list = new ArrayList<>();
            records.forEach(list::add);

            assertThat(list).isNotEmpty();
            assertThat(list.get(0).key()).isEqualTo("order-event-1");
            assertThat(list.get(0).value()).contains("\"orderId\":1");
        }

        assertThat(outboxEvent.isPublished()).isTrue();
    }

    @Test
    @DisplayName("여러 OutboxEvent를 한 번에 발행하고 모두 발행 완료 처리한다")
    void pollAndPublishMultiple() {
        OutboxEvent event1 = OutboxEvent.builder()
                .entityName("order.poll.multi")
                .entityId("evt-1")
                .payload("{\"orderId\":1}")
                .build();
        OutboxEvent event2 = OutboxEvent.builder()
                .entityName("order.poll.multi")
                .entityId("evt-2")
                .payload("{\"orderId\":2}")
                .build();

        given(outboxEventRepository.findByIsPublishedFalse()).willReturn(List.of(event1, event2));

        eventPoller.pollAndPublish();

        try (Consumer<String, String> consumer = createConsumer("order.poll.multi")) {
            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
            List<ConsumerRecord<String, String>> list = new ArrayList<>();
            records.forEach(list::add);
            assertThat(list).hasSizeGreaterThanOrEqualTo(2);
        }

        assertThat(event1.isPublished()).isTrue();
        assertThat(event2.isPublished()).isTrue();
    }
}
