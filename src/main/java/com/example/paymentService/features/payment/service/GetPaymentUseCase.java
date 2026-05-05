package com.example.paymentService.features.payment.service;

import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.features.payment.repository.PaymentRepository;
import com.example.paymentService.shared.exception.PaymentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ==========================================
 * GET PAYMENT USE CASE
 * ==========================================
 * 
 * Simple fetch of payment by ID.
 * 
 * NO AUTHORIZATION - Use GetPaymentWithAuthorizationUseCase for that.
 */
@Service
public class GetPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetPaymentUseCase.class);

    private final PaymentRepository paymentRepository;

    public GetPaymentUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment execute(String paymentId) {
        log.debug("Fetching payment: {}", paymentId);

        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment with ID " + paymentId + " not found"));
    }
}
