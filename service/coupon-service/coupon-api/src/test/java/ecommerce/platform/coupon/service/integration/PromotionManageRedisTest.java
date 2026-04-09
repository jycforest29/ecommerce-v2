package ecommerce.platform.coupon.service.integration;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.coupon.dto.PromotionRegisterRequest;
import ecommerce.platform.coupon.entity.Promotion;
import ecommerce.platform.coupon.repository.PromotionRepository;
import ecommerce.platform.coupon.service.PromotionManageService;
import ecommerce.platform.coupon.util.RedisKeyConverterUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * Redis(localhost:6379)에 직접 연결하여 프로모션 등록/삭제 시 Redis 초기화를 검증하는 통합 테스트.
 * docker-compose up 상태에서 실행해야 합니다.
 */
@ExtendWith(MockitoExtension.class)
class PromotionManageRedisTest {

    private PromotionManageService promotionManageService;

    @Mock
    private PromotionRepository promotionRepository;

    private RedisTemplate<String, Object> redisTemplate;
    private LettuceConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() {
        connectionFactory = new LettuceConnectionFactory("localhost", 6379);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();

        promotionManageService = new PromotionManageService(promotionRepository, redisTemplate);
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete("A::OUTER::FIXED_REG_TEST");
        redisTemplate.delete("ALL::ALL::RANDOM_REG_TEST::random");
        connectionFactory.destroy();
    }

    @Nested
    @DisplayName("프로모션 등록 - Redis 초기화")
    class Register {

        @Test
        @DisplayName("고정 할인 프로모션 등록 시 Redis에 수량이 SET된다")
        void registerFixedSetsQuantity() {
            PromotionRegisterRequest request = new PromotionRegisterRequest(
                    "FIXED_REG_TEST", 50, 30, 10, false, 0, 0,
                    10000, 5000,
                    Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
                    Category.OUTER, Brand.A
            );

            given(promotionRepository.save(any(Promotion.class))).willAnswer(inv -> {
                Promotion p = inv.getArgument(0);
                ReflectionTestUtils.setField(p, "promotionId", 1L);
                return p;
            });

            promotionManageService.register(request);

            Object value = redisTemplate.opsForValue().get("A::OUTER::FIXED_REG_TEST");
            assertThat(((Number) value).intValue()).isEqualTo(50);
        }

        @Test
        @DisplayName("랜덤 할인 프로모션 등록 시 Redis 리스트에 수량만큼 할인율이 RPUSH된다")
        void registerRandomPushesToList() {
            PromotionRegisterRequest request = new PromotionRegisterRequest(
                    "RANDOM_REG_TEST", 5, 30, 0, true, 5, 20,
                    10000, 5000,
                    Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
                    Category.ALL, Brand.ALL
            );

            given(promotionRepository.save(any(Promotion.class))).willAnswer(inv -> {
                Promotion p = inv.getArgument(0);
                ReflectionTestUtils.setField(p, "promotionId", 1L);
                return p;
            });

            promotionManageService.register(request);

            String listKey = "ALL::ALL::RANDOM_REG_TEST::random";
            Long size = redisTemplate.opsForList().size(listKey);
            assertThat(size).isEqualTo(5);

            // 각 값이 5~20 범위인지 확인
            for (int i = 0; i < 5; i++) {
                Object val = redisTemplate.opsForList().index(listKey, i);
                assertThat(((Number) val).intValue()).isBetween(5, 20);
            }
        }
    }

    @Nested
    @DisplayName("프로모션 삭제 - Redis 키 정리")
    class Remove {

        @Test
        @DisplayName("삭제 시 고정 할인 키와 랜덤 키 모두 삭제된다")
        void removeDeletesBothKeys() {
            Promotion promotion = Promotion.builder()
                    .promotionName("FIXED_REG_TEST")
                    .quantity(50)
                    .expireDays(30)
                    .discountRate(10)
                    .randomDiscount(false)
                    .minDiscountRate(0)
                    .maxDiscountRate(0)
                    .minPurchaseAmount(10000)
                    .maxDiscountAmount(5000)
                    .startedAt(Instant.now())
                    .endedAt(Instant.now().plus(30, ChronoUnit.DAYS))
                    .category(Category.OUTER)
                    .brand(Brand.A)
                    .build();
            ReflectionTestUtils.setField(promotion, "promotionId", 1L);

            String key = RedisKeyConverterUtil.toKey(promotion);
            redisTemplate.opsForValue().set(key, 50);
            redisTemplate.opsForList().rightPush(key + "::random", 10);

            given(promotionRepository.findById(1L)).willReturn(Optional.of(promotion));

            promotionManageService.remove(1L);

            assertThat(redisTemplate.hasKey(key)).isFalse();
            assertThat(redisTemplate.hasKey(key + "::random")).isFalse();
        }
    }
}