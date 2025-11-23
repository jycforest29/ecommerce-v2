package ecommerce.platform.coupon.util;

import ecommerce.platform.coupon.entity.Promotion;

public class RedisKeyConverterUtil {

    public static String toKey(Promotion promotion) {
        return "%s::%s::%s".formatted(promotion.getBrand(), promotion.getCategory(), promotion.getPromotionName());
    }
}
