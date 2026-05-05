package com.example.paymentService.features.payment.dto;

/**
 * ==========================================
 * CREATE PAYMENT REQUEST DTO
 * ==========================================
 * 
 * Data Transfer Object for creating a new payment.
 * 
 * SECURITY:
 * - Does NOT contain userId (prevents spoofing)
 * - userId is extracted from authentication token in controller
 * - userId is passed separately to use case
 * 
 * FIELDS:
 * - payerAlias: Payer's mobile number in international format (e.g.,
 * 46712345678)
 * - packageId: ID of the package being purchased
 * 
 * USED BY:
 * - PaymentController: Receives from frontend, adds userId from auth
 * - InitiatePaymentUseCase: Business logic for payment creation
 */
public class CreatePaymentRequestDTO {

    private String payerAlias; // Mobile number (Swish format)
    private Long packageId; // Package being purchased

    // Getters & Setters

    public String getPayerAlias() {
        return payerAlias;
    }

    public void setPayerAlias(String payerAlias) {
        this.payerAlias = payerAlias;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }
}