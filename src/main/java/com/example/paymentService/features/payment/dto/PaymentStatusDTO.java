package com.example.paymentService.features.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ==========================================
 * PAYMENT STATUS DTO
 * ==========================================
 * 
 * Data Transfer Object for payment status queries.
 * 
 * Used when retrieving payment details via GET /api/payments/{id}.
 * 
 * FIELDS:
 * - id: Payment unique identifier
 * - userId: User who created the payment
 * - packageId: Package being purchased
 * - amount: Payment amount
 * - status: Current payment status (CREATED, PENDING, PAID, DECLINED, ERROR,
 * CANCELLED)
 * - paymentMethod: Payment method (SWISH, MANUAL)
 * - payerAlias: Payer's mobile number (for Swish payments)
 * - paymentReference: Payment reference from Swish (when PAID)
 * - paidAt: Timestamp when payment was completed
 * - createdAt: Timestamp when payment was created
 * - expiresAt: Timestamp when payment expires
 * - errorCode: Error code if payment failed
 * - errorMessage: Error message if payment failed
 * 
 * CLEAN ARCHITECTURE:
 * - Controllers return DTOs, never entities
 * - Separates API contract from database model
 */
public class PaymentStatusDTO {

    private String id;
    private Long userId;
    private Long packageId;
    private BigDecimal amount;
    private String status;
    private String paymentMethod;
    private String payerAlias;
    private String paymentReference;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String errorCode;
    private String errorMessage;

    // Getters & Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPayerAlias() {
        return payerAlias;
    }

    public void setPayerAlias(String payerAlias) {
        this.payerAlias = payerAlias;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
