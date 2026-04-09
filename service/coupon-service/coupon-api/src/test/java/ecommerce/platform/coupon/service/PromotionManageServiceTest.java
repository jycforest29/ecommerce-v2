package ecommerce.platform.coupon.service;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.coupon.dto.PromotionRegisterRequest;
import ecommerce.platform.coupon.dto.PromotionRegisterResponse;
import ecommerce.platform.coupon.entity.Promotion;
import ecommerce.platform.coupon.repository.PromotionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionManageServiceTest {

    @InjectMocks
    private PromotionManageService promotionManageService;

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private RedisTemplate<String, Object> couponRedisRepository;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ListOperations<String, Object> listOperations;

    @Nested
    @DisplayName("프로모션 등록 - register")
    class Register {

        @Test
        @DisplayName("고정 할인 프로모션 등록 시 Redis에 수량을 SET한다")
        void registerFixedDiscount() {
            PromotionRegisterRequest request = new PromotionRegisterRequest(
                    "FIXED_PROMO", 50, 30, 10, false, 0, 0,
                    10000, 5000,
                    Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
                    Category.OUTER, Brand.A
            );

            given(promotionRepository.save(any(Promotion.class))).willAnswer(invocation -> {
                Promotion p = invocation.getArgument(0);
                ReflectionTestUtils.setField(p, "promotionId", 1L);
                return p;
            });
            given(couponRedisRepository.opsForValue()).willReturn(valueOperations);

            PromotionRegisterResponse response = promotionManageService.register(request);

            assertThat(response.promotionName()).isEqualTo("FIXED_PROMO");
            assertThat(response.quantity()).isEqualTo(50);
            verify(valueOperations).set("A::OUTER::FIXED_PROMO", 50);
        }

        @Test
        @DisplayName("랜덤 할인 프로모션 등록 시 Redis에 수량만큼 RPUSH한다")
        void registerRandomDiscount() {
            PromotionRegisterRequest request = new PromotionRegisterRequest(
                    "RANDOM_PROMO", 3, 30, 0, true, 5, 20,
                    10000, 5000,
                    Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS),
                    Category.ALL, Brand.ALL
            );

            given(promotionRepository.save(any(Promotion.class))).willAnswer(invocation -> {
                Promotion p = invocation.getArgument(0);
                ReflectionTestUtils.setField(p, "promotionId", 1L);
                return p;
            });
            given(couponRedisRepository.opsForList()).willReturn(listOperations);

            promotionManageService.register(request);

            verify(listOperations, times(3)).rightPush(eq("ALL::ALL::RANDOM_PROMO::random"), anyInt());
        }
    }

    @Nested
    @DisplayName("프로모션 삭제 - remove")
    class Remove {

        @Test
        @DisplayName("프로모션과 Redis 키를 함께 삭제한다")
        void removeSuccess() {
            Promotion promotion = Promotion.builder()
                    .promotionName("TEST_PROMO")
                    .quantity(100)
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

            given(promotionRepository.findById(1L)).willReturn(Optional.of(promotion));
            given(couponRedisRepository.delete("A::OUTER::TEST_PROMO")).willReturn(true);
            given(couponRedisRepository.delete("A::OUTER::TEST_PROMO::random")).willReturn(true);

            promotionManageService.remove(1L);

            verify(couponRedisRepository).delete("A::OUTER::TEST_PROMO");
            verify(couponRedisRepository).delete("A::OUTER::TEST_PROMO::random");
            verify(promotionRepository).delete(promotion);
        }

        @Test
        @DisplayName("존재하지 않는 프로모션 삭제 시 EntityNotFoundException 발생")
        void removeFail_notFound() {
            given(promotionRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> promotionManageService.remove(999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
