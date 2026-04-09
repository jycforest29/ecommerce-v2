package ecommerce.platform.payment.service;

import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.common.exception.UnauthorizedAccessException;
import ecommerce.platform.common.util.EntityFinder;
import ecommerce.platform.payment.dto.PaymentQueryResponse;
import ecommerce.platform.payment.entity.Payment;
import ecommerce.platform.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;

    public Page<PaymentQueryResponse> queryPayments(Long userId, Pageable pageable) {
        return paymentRepository.findAllByUserId(userId, pageable)
                .map(PaymentQueryResponse::from);
    }

    public PaymentQueryResponse queryPayment(Long userId, Long paymentId) {
        Payment payment = EntityFinder.findEntity(paymentRepository, paymentId);
        if (!payment.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException();
        }
        return PaymentQueryResponse.from(payment);
    }

    public PaymentQueryResponse queryPaymentByOrderId(Long userId, Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("payment"));
        if (!payment.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException();
        }
        return PaymentQueryResponse.from(payment);
    }
}