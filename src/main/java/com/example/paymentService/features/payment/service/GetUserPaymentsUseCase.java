package com.example.paymentService.features.payment.service;

import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.features.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ==========================================
 * GET USER PAYMENTS USE CASE
 * ==========================================
 * 
 * Fetch all payments for a specific user.
 * 
 * USED BY:
 * - Admin: View user's payment history
 * - User: View own payment history
 */
@Service
public class GetUserPaymentsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetUserPaymentsUseCase.class);

    private final PaymentRepository paymentRepository;

    public GetUserPaymentsUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> execute(Long userId) {
        log.debug("Fetching payments for userId: {}", userId);
        List<Payment> payments = paymentRepository.findByUserId(userId);
        log.debug("Found {} payments for user {}", payments.size(), userId);
        return payments;
    }
}
