package com.example.paymentService.swish.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/**
 * DTO för response från Swish (GET /api/v1/paymentrequests/{id} och Callbacks)
 * 
 * Detta är objektet som Swish returnerar när vi:
 * - Hämtar status på en betalning (GET)
 * - Får en callback från Swish efter betalning
 * 
 * Innehåller ALLA fält som Swish kan returnera.
 * 
 * Enligt Swish dokumentation v1/v2.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SwishPaymentResponse {

    // Response-fält från Swish
    private String id; // Payment request ID (instructionUUID)
    private String payeePaymentReference; // Order reference
    private String paymentReference; // Swish payment reference (endast vid PAID)
    private String callbackUrl; // Callback URL
    private String payerAlias; // Customer phone number
    private String payeeAlias; // Merchant Swish number
    private BigDecimal amount; // Belopp
    private String currency; // Currency (SEK)
    private String message; // Meddelande

    // Status och tidsstämplar
    private String status; // PAID, DECLINED, ERROR, CANCELLED
    private String dateCreated; // När skapad (ISO 8601)
    private String datePaid; // När betald (ISO 8601)

    // Felhantering
    private String errorCode; // Swish error code (FF08, RP03, etc.)
    private String errorMessage; // Felmeddelande

    // Callback-validering
    private String callbackIdentifier; // För att validera callback

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayeePaymentReference() {
        return payeePaymentReference;
    }

    public void setPayeePaymentReference(String payeePaymentReference) {
        this.payeePaymentReference = payeePaymentReference;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getPayerAlias() {
        return payerAlias;
    }

    public void setPayerAlias(String payerAlias) {
        this.payerAlias = payerAlias;
    }

    public String getPayeeAlias() {
        return payeeAlias;
    }

    public void setPayeeAlias(String payeeAlias) {
        this.payeeAlias = payeeAlias;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDatePaid() {
        return datePaid;
    }

    public void setDatePaid(String datePaid) {
        this.datePaid = datePaid;
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

    public String getCallbackIdentifier() {
        return callbackIdentifier;
    }

    public void setCallbackIdentifier(String callbackIdentifier) {
        this.callbackIdentifier = callbackIdentifier;
    }
}
