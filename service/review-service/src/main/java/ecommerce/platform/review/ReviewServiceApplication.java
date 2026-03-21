package ecommerce.platform.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "ecommerce.platform")
@EntityScan("ecommerce.platform")
@EnableCaching
public class ReviewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }
}
