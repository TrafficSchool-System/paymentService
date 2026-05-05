package com.example.paymentService.features.payment.service;

import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.features.payment.entity.PaymentStatus;
import com.example.paymentService.features.payment.repository.PaymentRepository;
import com.example.paymentService.shared.exception.PaymentNotFoundException;
import com.example.paymentService.features.payment.client.swish.dto.SwishPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * ==========================================
 * HANDLE SWISH CALLBACK USE CASE
 * ==========================================
 * 
 * Business logic for processing Swish payment callbacks.
 * 
 * RESPONSIBILITIES:
 * - Receive Swish callback (PAID/DECLINED/ERROR/CANCELLED)
 * - Update payment status
 * - Trigger subscription activation on PAID
 * - Handle idempotency (ignore duplicate callbacks)
 * 
 * FLOW:
 * 1. Find payment by ID
 * 2. Check if already processed (idempotency)
 * 3. Switch on Swish status:
 * - PAID → Activate subscription → Update payment
 * - DECLINED → Update status
 * - ERROR → Log error, update status
 * - CANCELLED → Update status
 * 4. Save updated payment
 * 
 * CRITICAL:
 * - Subscription MUST be activated BEFORE marking payment as PAID
 * - If subscription fails, payment marked as ERROR (manual intervention needed)
 */
@Service
public class HandleSwishCallbackUseCase {

    private static final Logger log = LoggerFactory.getLogger(HandleSwishCallbackUseCase.class);

    private final PaymentRepository paymentRepository;
    private final ActivateSubscriptionUseCase activateSubscriptionUseCase;

    public HandleSwishCallbackUseCase(
            PaymentRepository paymentRepository,
            ActivateSubscriptionUseCase activateSubscriptionUseCase) {
        this.paymentRepository = paymentRepository;
        this.activateSubscriptionUseCase = activateSubscriptionUseCase;
    }

    @Transactional
    public void execute(SwishPaymentResponse swishCallback) {
        log.info("Handling Swish callback for payment ID: {}", swishCallback.getId());

        // 1. Find payment in database
        Payment payment = paymentRepository.findById(swishCallback.getId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment with ID " + swishCallback.getId() + " not found"));

        log.info("Payment found: Status before={}, Amount={}",
                payment.getStatus(), payment.getAmount());

        // 2. Check if payment already processed (idempotency)
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.warn("Payment is already PAID, ignoring callback");
            return;
        }

        // 3. Process based on Swish status
        String swishStatus = swishCallback.getStatus();

        switch (swishStatus) {
            case "PAID":
                handlePaidStatus(payment, swishCallback);
                break;

            case "DECLINED":
                log.warn("Payment declined by user");
                payment.setStatus(PaymentStatus.DECLINED);
                break;

            case "ERROR":
                log.error("Error in payment: {} - {}",
                        swishCallback.getErrorCode(), swishCallback.getErrorMessage());
                payment.setStatus(PaymentStatus.ERROR);
                payment.setErrorCode(swishCallback.getErrorCode());
                payment.setErrorMessage(swishCallback.getErrorMessage());
                break;

            case "CANCELLED":
                log.info("Payment cancelled");
                payment.setStatus(PaymentStatus.CANCELLED);
                break;

            default:
                log.warn("Unknown Swish status: {}", swishStatus);
                return;
        }

        // 4. Save updated payment
        paymentRepository.save(payment);
        log.info("Payment updated to status: {}", payment.getStatus());
    }

    private void handlePaidStatus(Payment payment, SwishPaymentResponse swishCallback) {
        log.info("Payment completed!");

        // CRITICAL: Activate subscription BEFORE marking as PAID
        // If subscription fails, payment stays in ERROR state
        try {
            activateSubscriptionUseCase.execute(payment);

            // Subscription activated - NOW mark as PAID
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            payment.setPaymentReference(swishCallback.getPaymentReference());

            // TODO: Future enhancements
            // - Send receipt via email
            // - Log for accounting

        } catch (Exception e) {
            // Subscription activation failed - mark as ERROR
            log.error("CRITICAL: Payment received but subscription activation failed for payment {}",
                    payment.getId(), e);
            payment.setStatus(PaymentStatus.ERROR);
            payment.setErrorCode("SUBSCRIPTION_FAILED");
            payment.setErrorMessage("Subscription activation failed: " + e.getMessage());
            payment.setPaymentReference(swishCallback.getPaymentReference());
        }
    }
}
