package com.example.paymentService.features.payment.service;

import com.example.paymentService.features.packages.entity.Package;
import com.example.paymentService.features.packages.repository.PackageRepository;
import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.features.payment.client.UserServiceClient;
import com.example.paymentService.shared.exception.PackageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ==========================================
 * ACTIVATE SUBSCRIPTION USE CASE
 * ==========================================
 * 
 * Business logic for activating user subscriptions after successful payment.
 * 
 * RESPONSIBILITIES:
 * - Fetch package details
 * - Call UserService to create subscription
 * - Handle external integration errors
 * 
 * SEPARATION OF CONCERNS:
 * - Use case: Orchestration logic (what to do)
 * - UserServiceClient: External integration (how to do it)
 * 
 * CRITICAL:
 * - Called BEFORE payment is marked as PAID
 * - Throws exception if activation fails
 * - Prevents "paid but no access" scenario
 */
@Service
public class ActivateSubscriptionUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActivateSubscriptionUseCase.class);

    private final PackageRepository packageRepository;
    private final UserServiceClient userServiceClient;

    public ActivateSubscriptionUseCase(
            PackageRepository packageRepository,
            UserServiceClient userServiceClient) {
        this.packageRepository = packageRepository;
        this.userServiceClient = userServiceClient;
    }

    /**
     * Activate subscription for user after successful payment.
     * 
     * @param payment Paid payment entity
     * @throws PackageNotFoundException if package not found
     * @throws RuntimeException         if UserService call fails
     */
    public void execute(Payment payment) {
        log.info("🔄 Activating subscription for payment: {}", payment.getId());

        // Fetch package details
        Package pkg = packageRepository.findById(payment.getPackageId())
                .orElseThrow(() -> new PackageNotFoundException(
                        "Package not found: " + payment.getPackageId()));

        // Delegate external integration to client
        userServiceClient.activateSubscription(payment, pkg);

        log.info("✅ Subscription activated for userId={}", payment.getUserId());
    }
}
