package com.example.paymentService.features.payment.service;

import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.features.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ==========================================
 * DELETE USER PAYMENTS USE CASE
 * ==========================================
 * 
 * Cascade delete all payments for a user.
 * 
 * USED BY: UserService when deleting a user account
 * 
 * WARNING:
 * - Permanently deletes payment history
 * - Consider backup/archival for accounting purposes
 */
@Service
public class DeleteUserPaymentsUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteUserPaymentsUseCase.class);

    private final PaymentRepository paymentRepository;

    public DeleteUserPaymentsUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void execute(Long userId) {
        log.info("Cascade delete: Removing all payments for userId: {}", userId);

        List<Payment> payments = paymentRepository.findByUserId(userId);

        if (!payments.isEmpty()) {
            paymentRepository.deleteAll(payments);
            log.info("Deleted {} payments for user {}", payments.size(), userId);
        } else {
            log.info("No payments to delete for user {}", userId);
        }
    }
}
