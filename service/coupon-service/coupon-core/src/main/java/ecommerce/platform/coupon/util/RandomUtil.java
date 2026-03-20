package ecommerce.platform.coupon.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {

    public static int random(int start, int end) {
        return ThreadLocalRandom.current().nextInt(start, end);
    }
}
