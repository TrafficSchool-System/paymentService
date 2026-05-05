package com.example.paymentService.features.payment.integration;

import com.example.paymentService.features.payment.client.swish.dto.SwishPaymentRequest;
import org.springframework.http.ResponseEntity;

/**
 * ==========================================
 * SWISH PAYMENT GATEWAY
 * ==========================================
 * 
 * Gateway abstraction for Swish payment provider integration.
 * 
 * PURPOSE:
 * - Decouples business logic from external Swish client
 * - Makes payment use cases testable (mock this gateway)
 * - Isolates third-party dependency changes
 * 
 * PATTERN:
 * - Gateway Pattern (Domain-Driven Design)
 * - Adapter Pattern (Clean Architecture)
 * 
 * USAGE:
 * ```java
 * 
 * @Service
 *          public class InitiatePaymentUseCase {
 *          private final SwishPaymentGateway swishGateway;
 * 
 *          public void execute(...) {
 *          swishGateway.createPayment(paymentId, swishRequest);
 *          }
 *          }
 *          ```
 */
public interface SwishPaymentGateway {

    /**
     * Create a new Swish payment request.
     * 
     * Delegates to external Swish payment provider to initiate
     * a new payment transaction.
     * 
     * @param paymentId    Unique payment identifier (instructionUUID)
     * @param swishRequest Swish payment request details
     * @return Swish API response with status and location
     * @throws RuntimeException if Swish call fails
     */
    ResponseEntity<String> createPayment(String paymentId, SwishPaymentRequest swishRequest);
}
