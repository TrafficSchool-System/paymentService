package com.example.paymentService.features.payment.service;

import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.features.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ==========================================
 * GET ALL PAYMENTS USE CASE
 * ==========================================
 * 
 * Fetch all payments in system.
 * 
 * USED BY: Admin dashboard for monitoring
 */
@Service
public class GetAllPaymentsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetAllPaymentsUseCase.class);

    private final PaymentRepository paymentRepository;

    public GetAllPaymentsUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> execute() {
        log.info("[ADMIN] Fetching all payments");
        return paymentRepository.findAll();
    }
}
