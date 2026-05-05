package com.example.paymentService.features.payment.client;

import com.example.paymentService.features.payment.client.dto.CreateSubscriptionRequestDTO;
import com.example.paymentService.features.packages.entity.Package;
import com.example.paymentService.features.payment.entity.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * ==========================================
 * USER SERVICE CLIENT
 * ==========================================
 * 
 * External integration with UserService for subscription activation.
 * 
 * RESPONSIBILITIES:
 * - Build subscription activation requests
 * - Call UserService REST API
 * - Handle API key authentication
 * - Throw exceptions on failures
 * 
 * PATTERN:
 * - Separates external integration from business logic
 * - Makes use cases testable (mock this client)
 * - Centralizes UserService communication
 * 
 * USAGE:
 * ```java
 * 
 * @Service
 *          public class ActivateSubscriptionUseCase {
 *          private final UserServiceClient userServiceClient;
 * 
 *          public void execute(Payment payment, Package pkg) {
 *          userServiceClient.activateSubscription(payment, pkg);
 *          }
 *          }
 *          ```
 */
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final WebClient userServiceWebClient;

    @Value("${service.api.key}")
    private String serviceApiKey;

    public UserServiceClient(WebClient userServiceWebClient) {
        this.userServiceWebClient = userServiceWebClient;
    }

    /**
     * Activate subscription in UserService after successful payment.
     * 
     * CRITICAL OPERATION:
     * - Called after payment is confirmed (PAID)
     * - Grants user access to packages
     * - Throws exception if activation fails
     * 
     * @param payment Paid payment entity
     * @param pkg     Package being purchased
     * @throws RuntimeException if subscription activation fails
     */
    public void activateSubscription(Payment payment, Package pkg) {
        log.info("🔄 Activating subscription in UserService for payment: {}", payment.getId());

        // Build subscription request
        CreateSubscriptionRequestDTO subscriptionRequest = new CreateSubscriptionRequestDTO();
        subscriptionRequest.setUserId(payment.getUserId());
        subscriptionRequest.setPackageId(pkg.getId());
        subscriptionRequest.setPackageName(pkg.getName());
        subscriptionRequest.setPackagePrice(pkg.getPrice());
        subscriptionRequest.setValidityDays(pkg.getValidityDays());
        subscriptionRequest.setValidityHours(pkg.getValidityHours());
        subscriptionRequest.setPaymentId(payment.getId());

        // Call UserService with API key authentication
        String response = userServiceWebClient.post()
                .uri("/api/subscriptions")
                .header("X-Internal-API-Key", serviceApiKey)
                .bodyValue(subscriptionRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("✅ Subscription activated in UserService for userId={}, response: {}",
                payment.getUserId(), response);
    }
}
