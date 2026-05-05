package com.example.paymentService.shared.exception;

/**
 * ==========================================
 * SUBSCRIPTION ACTIVATION EXCEPTION
 * ==========================================
 * 
 * Thrown when subscription activation fails in UserService.
 * 
 * USED WHEN:
 * - Payment is PAID but UserService call fails
 * - Network error when calling UserService
 * - UserService returns error response
 * 
 * CRITICAL SCENARIO:
 * - Payment marked as PAID in PaymentService
 * - But subscription NOT activated in UserService
 * - Requires manual intervention or retry mechanism
 * 
 * RECOVERY STRATEGY:
 * - Log payment ID for manual recovery
 * - Admin must manually activate subscription
 * - Consider implementing retry queue or dead-letter queue
 */
public class SubscriptionActivationException extends RuntimeException {

    public SubscriptionActivationException(String message) {
        super(message);
    }

    public SubscriptionActivationException(String message, Throwable cause) {
        super(message, cause);
    }
}
