package ecommerce.platform.coupon.entity;

import ecommerce.platform.common.constants.Brand;
import ecommerce.platform.common.constants.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CouponTargetItem {
    private Brand brand;
    private Category category;
    private int quantity;
    private int price;
}
