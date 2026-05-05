package com.example.paymentService.features.payment.entity;

/**
 * Payment method enum
 * 
 * SWISH: Payment via Swish integration (requires callback, payerAlias, etc.)
 * MANUAL: Manual payment created by admin (no callback, marked as PAID
 * immediately)
 */
public enum PaymentMethod {
    SWISH, // Standard Swish payment flow
    MANUAL // Admin-created payment (offline, in-person, etc.)
}
