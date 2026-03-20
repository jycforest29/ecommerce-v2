package ecommerce.platform.payment.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/pg/mock")
public class PGMockController {

    private static final double SUCCESS_RATE = 0.7;

    @PostMapping("/payment")
    public PGResponse processPayment() {
        boolean success = ThreadLocalRandom.current().nextDouble() < SUCCESS_RATE;
        return success
                ? PGResponse.success("결제 승인 완료")
                : PGResponse.fail("결제 승인 실패");
    }

    @PostMapping("/refund")
    public PGResponse processRefund() {
        boolean success = ThreadLocalRandom.current().nextDouble() < SUCCESS_RATE;
        return success
                ? PGResponse.success("환불 처리 완료")
                : PGResponse.fail("환불 처리 실패");
    }

    public record PGResponse(boolean success, String message) {
        public static PGResponse success(String message) {
            return new PGResponse(true, message);
        }

        public static PGResponse fail(String message) {
            return new PGResponse(false, message);
        }
    }
}