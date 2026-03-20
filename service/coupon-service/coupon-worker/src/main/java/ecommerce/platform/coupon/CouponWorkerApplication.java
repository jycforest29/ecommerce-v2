package ecommerce.platform.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CouponWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouponWorkerApplication.class, args);
    }
}
