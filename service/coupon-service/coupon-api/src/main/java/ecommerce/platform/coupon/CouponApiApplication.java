package ecommerce.platform.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "ecommerce.platform")
@EntityScan("ecommerce.platform")
public class CouponApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouponApiApplication.class, args);
    }
}
