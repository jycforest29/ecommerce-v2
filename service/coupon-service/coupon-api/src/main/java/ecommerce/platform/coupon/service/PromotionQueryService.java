package ecommerce.platform.coupon.service;

import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.coupon.dto.PromotionQueryResponse;
import ecommerce.platform.coupon.entity.Promotion;
import ecommerce.platform.coupon.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionQueryService {
    private final PromotionRepository promotionRepository;

    public List<PromotionQueryResponse> getAllPromotions() {
        return promotionRepository.findAll()
                .stream()
                .map(PromotionQueryResponse::from)
                .toList();
    }

    public PromotionQueryResponse getPromotion(Long promotionId) {
        Promotion promotion = EntityFinder.findEntity(promotionRepository, promotionId);
        return PromotionQueryResponse.from(promotion);
    }
}
