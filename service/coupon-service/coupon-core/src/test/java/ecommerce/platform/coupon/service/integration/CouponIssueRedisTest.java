package ecommerce.platform.coupon.service.integration;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.coupon.entity.Coupon;
import ecommerce.platform.coupon.entity.Promotion;
import ecommerce.platform.coupon.exception.CouponAlreadyIssuedException;
import ecommerce.platform.coupon.exception.CouponSoldOutException;
import ecommerce.platform.coupon.repository.CouponLogRepository;
import ecommerce.platform.coupon.repository.CouponRepository;
import ecommerce.platform.coupon.repository.PromotionRepository;
import ecommerce.platform.coupon.service.CouponIssueService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * Redis(localhost:6379)에 직접 연결하여 쿠폰 발급 로직을 검증하는 통합 테스트.
 * docker-compose up 상태에서 실행해야 합니다.
 */
@ExtendWith(MockitoExtension.class)
class CouponIssueRedisTest {

    private CouponIssueService couponIssueService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponLogRepository couponLogRepository;

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

        couponIssueService = new CouponIssueService(
                couponRepository, couponLogRepository, promotionRepository, redisTemplate);
    }

    @AfterEach
    void tearDown() {
        // 테스트에서 사용한 키 정리
        redisTemplate.delete("A::OUTER::FIXED_TEST");
        redisTemplate.delete("ALL::ALL::RANDOM_TEST::random");
        connectionFactory.destroy();
    }

    private Promotion createFixedPromotion() {
        Promotion promotion = Promotion.builder()
                .promotionName("FIXED_TEST")
                .quantity(3)
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
        return promotion;
    }

    private Promotion createRandomPromotion() {
        Promotion promotion = Promotion.builder()
                .promotionName("RANDOM_TEST")
                .quantity(3)
                .expireDays(30)
                .discountRate(0)
                .randomDiscount(true)
                .minDiscountRate(5)
                .maxDiscountRate(20)
                .minPurchaseAmount(10000)
                .maxDiscountAmount(5000)
                .startedAt(Instant.now())
                .endedAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .category(Category.ALL)
                .brand(Brand.ALL)
                .build();
        ReflectionTestUtils.setField(promotion, "promotionId", 2L);
        return promotion;
    }

    private void stubCommonMocks(Promotion promotion, Long userId) {
        given(promotionRepository.findById(promotion.getPromotionId())).willReturn(Optional.of(promotion));
        given(couponRepository.existsByPromotionAndUserId(promotion, userId)).willReturn(false);
        given(couponRepository.save(any(Coupon.class))).willAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("고정 할인 - Redis DECR/INCR")
    class FixedDiscount {

        @Test
        @DisplayName("DECR로 재고를 차감하고 쿠폰을 발급한다")
        void decrReducesStock() {
            Promotion promotion = createFixedPromotion();
            String key = RedisKeyConverterUtil.toKey(promotion);
            redisTemplate.opsForValue().set(key, 3);

            stubCommonMocks(promotion, 1L);

            var response = couponIssueService.issuePromotion(1L, 1L);

            assertThat(response.discountRate()).isEqualTo(10);

            Object remaining = redisTemplate.opsForValue().get(key);
            assertThat(((Number) remaining).longValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("재고를 연속 차감하면 순차적으로 줄어든다")
        void consecutiveDecr() {
            Promotion promotion = createFixedPromotion();
            String key = RedisKeyConverterUtil.toKey(promotion);
            redisTemplate.opsForValue().set(key, 3);

            for (long userId = 1; userId <= 3; userId++) {
                given(promotionRepository.findById(1L)).willReturn(Optional.of(promotion));
                given(couponRepository.existsByPromotionAndUserId(promotion, userId)).willReturn(false);
                given(couponRepository.save(any(Coupon.class))).willAnswer(inv -> inv.getArgument(0));

                couponIssueService.issuePromotion(1L, userId);
            }

            Object remaining = redisTemplate.opsForValue().get(key);
            assertThat(((Number) remaining).longValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("재고 소진 시 INCR로 롤백하고 CouponSoldOutException을 던진다")
        void soldOutRollsBack() {
            Promotion promotion = createFixedPromotion();
            String key = RedisKeyConverterUtil.toKey(promotion);
            redisTemplate.opsForValue().set(key, 0);

            stubCommonMocks(promotion, 1L);

            assertThatThrownBy(() -> couponIssueService.issuePromotion(1L, 1L))
                    .isInstanceOf(CouponSoldOutException.class);

            // INCR로 복구되어 0으로 돌아와야 함
            Object remaining = redisTemplate.opsForValue().get(key);
            assertThat(((Number) remaining).longValue()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("랜덤 할인 - Redis LPOP")
    class RandomDiscount {

        @Test
        @DisplayName("LPOP으로 미리 적재된 할인율을 꺼내 쿠폰을 발급한다")
        void lpopReturnsRate() {
            Promotion promotion = createRandomPromotion();
            String listKey = RedisKeyConverterUtil.toKey(promotion) + "::random";

            redisTemplate.opsForList().rightPush(listKey, 10);
            redisTemplate.opsForList().rightPush(listKey, 15);
            redisTemplate.opsForList().rightPush(listKey, 20);

            stubCommonMocks(promotion, 1L);

            var response = couponIssueService.issuePromotion(2L, 1L);

            assertThat(response.discountRate()).isEqualTo(10);

            // 리스트에 2개 남아야 함
            Long size = redisTemplate.opsForList().size(listKey);
            assertThat(size).isEqualTo(2);
        }

        @Test
        @DisplayName("LPOP은 FIFO 순서로 꺼낸다")
        void lpopFifoOrder() {
            Promotion promotion = createRandomPromotion();
            String listKey = RedisKeyConverterUtil.toKey(promotion) + "::random";

            redisTemplate.opsForList().rightPush(listKey, 5);
            redisTemplate.opsForList().rightPush(listKey, 15);

            stubCommonMocks(promotion, 1L);
            var first = couponIssueService.issuePromotion(2L, 1L);
            assertThat(first.discountRate()).isEqualTo(5);

            given(couponRepository.existsByPromotionAndUserId(promotion, 2L)).willReturn(false);
            var second = couponIssueService.issuePromotion(2L, 2L);
            assertThat(second.discountRate()).isEqualTo(15);
        }

        @Test
        @DisplayName("리스트가 비어있으면 CouponSoldOutException을 던진다")
        void soldOutWhenEmpty() {
            Promotion promotion = createRandomPromotion();
            // 리스트에 아무것도 안 넣음

            stubCommonMocks(promotion, 1L);

            assertThatThrownBy(() -> couponIssueService.issuePromotion(2L, 1L))
                    .isInstanceOf(CouponSoldOutException.class);
        }
    }
}
