package ecommerce.platform.review.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PurchaseVerifierTest {

    private PurchaseVerifier purchaseVerifier;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private SetOperations<String, Object> setOperations;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        purchaseVerifier = new PurchaseVerifier(redisTemplate);
    }

    @Test
    @DisplayName("구매 이력을 추가한다")
    void addPurchase() {
        purchaseVerifier.addPurchase(1L, 100L);

        then(setOperations).should().add("purchased:product:1", 100L);
    }

    @Test
    @DisplayName("구매 이력을 제거한다")
    void removePurchase() {
        purchaseVerifier.removePurchase(1L, 100L);

        then(setOperations).should().remove("purchased:product:1", 100L);
    }

    @Test
    @DisplayName("구매 이력이 있으면 true를 반환한다")
    void hasPurchasedTrue() {
        given(setOperations.isMember("purchased:product:1", 100L)).willReturn(true);

        assertThat(purchaseVerifier.hasPurchased(1L, 100L)).isTrue();
    }

    @Test
    @DisplayName("구매 이력이 없으면 false를 반환한다")
    void hasPurchasedFalse() {
        given(setOperations.isMember("purchased:product:1", 100L)).willReturn(false);

        assertThat(purchaseVerifier.hasPurchased(1L, 100L)).isFalse();
    }
}