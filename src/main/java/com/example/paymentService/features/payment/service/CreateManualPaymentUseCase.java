package com.example.paymentService.features.payment.service;

import com.example.paymentService.features.packages.entity.Package;
import com.example.paymentService.features.packages.repository.PackageRepository;
import com.example.paymentService.features.payment.dto.CreateManualPaymentDTO;
import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.features.payment.entity.PaymentMethod;
import com.example.paymentService.features.payment.entity.PaymentStatus;
import com.example.paymentService.features.payment.repository.PaymentRepository;
import com.example.paymentService.shared.exception.PackageNotFoundException;
import com.example.paymentService.shared.exception.SubscriptionActivationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ==========================================
 * CREATE MANUAL PAYMENT USE CASE
 * ==========================================
 * 
 * Business logic for creating manual payments by admin.
 * 
 * USED WHEN:
 * - Admin manually registers a payment (e.g., cash payment at school)
 * - Payment is immediately marked as PAID
 * - Subscription is activated automatically
 * 
 * RESPONSIBILITIES:
 * - Validate package
 * - Create payment entity (status: PAID, method: MANUAL)
 * - Activate subscription
 * - Return created payment
 */
@Service
public class CreateManualPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateManualPaymentUseCase.class);

    private final PaymentRepository paymentRepository;
    private final PackageRepository packageRepository;
    private final ActivateSubscriptionUseCase activateSubscriptionUseCase;

    public CreateManualPaymentUseCase(
            PaymentRepository paymentRepository,
            PackageRepository packageRepository,
            ActivateSubscriptionUseCase activateSubscriptionUseCase) {
        this.paymentRepository = paymentRepository;
        this.packageRepository = packageRepository;
        this.activateSubscriptionUseCase = activateSubscriptionUseCase;
    }

    @Transactional
    public Payment execute(CreateManualPaymentDTO request) {
        log.info("Creating manual payment for userId: {}, packageId: {}",
                request.getUserId(), request.getPackageId());

        // 1. Validate and fetch package
        Package pkg = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new PackageNotFoundException(
                        "Package with ID " + request.getPackageId() + " not found"));

        log.info("Package found: {} - Price: {} SEK", pkg.getName(), pkg.getPrice());

        // 2. Generate unique payment ID
        String paymentId = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .toUpperCase();

        // 3. Create Payment entity (status: PAID, method: MANUAL)
        Payment payment = Payment.builder()
                .id(paymentId)
                .userId(request.getUserId())
                .packageId(pkg.getId())
                .amount(pkg.getPrice())
                .status(PaymentStatus.PAID)
                .paymentMethod(PaymentMethod.MANUAL)
                .payerAlias("ADMIN") // Manual payment created by admin
                .paymentReference("MANUAL-" + System.currentTimeMillis())
                .callbackIdentifier("MANUAL")
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        // 4. Save payment
        Payment savedPayment = paymentRepository.save(payment);
        log.info("✅ Manual payment created: {}", paymentId);

        // 5. Activate subscription
        try {
            activateSubscriptionUseCase.execute(savedPayment);
        } catch (Exception e) {
            log.error("❌ Failed to activate subscription for payment {}: {}",
                    paymentId, e.getMessage());
            // Payment is already PAID, but subscription failed
            // This should be handled manually or via retry mechanism
            throw new SubscriptionActivationException(
                    "Payment " + paymentId + " created but subscription activation failed. " +
                            "Manual intervention required.",
                    e);
        }

        return savedPayment;
    }
}
