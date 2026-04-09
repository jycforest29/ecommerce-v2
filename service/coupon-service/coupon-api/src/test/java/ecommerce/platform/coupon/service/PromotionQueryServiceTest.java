package ecommerce.platform.coupon.service;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.coupon.dto.PromotionQueryResponse;
import ecommerce.platform.coupon.entity.Promotion;
import ecommerce.platform.coupon.repository.PromotionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PromotionQueryServiceTest {

    @InjectMocks
    private PromotionQueryService promotionQueryService;

    @Mock
    private PromotionRepository promotionRepository;

    private Promotion createPromotion(String name, Long id) {
        Promotion promotion = Promotion.builder()
                .promotionName(name)
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
        ReflectionTestUtils.setField(promotion, "promotionId", id);
        return promotion;
    }

    @Test
    @DisplayName("전체 프로모션 목록을 반환한다")
    void getAllPromotions() {
        given(promotionRepository.findAll()).willReturn(
                List.of(createPromotion("PROMO_1", 1L), createPromotion("PROMO_2", 2L))
        );

        List<PromotionQueryResponse> result = promotionQueryService.getAllPromotions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).promotionName()).isEqualTo("PROMO_1");
        assertThat(result.get(1).promotionName()).isEqualTo("PROMO_2");
    }

    @Test
    @DisplayName("프로모션이 없으면 빈 리스트를 반환한다")
    void getAllPromotionsEmpty() {
        given(promotionRepository.findAll()).willReturn(List.of());

        assertThat(promotionQueryService.getAllPromotions()).isEmpty();
    }

    @Test
    @DisplayName("단건 프로모션을 조회한다")
    void getPromotion() {
        given(promotionRepository.findById(1L)).willReturn(Optional.of(createPromotion("PROMO_1", 1L)));

        PromotionQueryResponse result = promotionQueryService.getPromotion(1L);

        assertThat(result.promotionName()).isEqualTo("PROMO_1");
    }

    @Test
    @DisplayName("존재하지 않는 프로모션 조회 시 EntityNotFoundException 발생")
    void getPromotionNotFound() {
        given(promotionRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> promotionQueryService.getPromotion(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
