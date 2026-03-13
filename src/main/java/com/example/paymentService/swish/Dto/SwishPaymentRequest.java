package com.example.paymentService.swish.Dto;

import java.math.BigDecimal;

/**
 * DTO för att skapa en betalning i Swish (PUT
 * /api/v2/paymentrequests/{instructionUUID})
 * 
 * Detta är request-objektet som skickas till Swish API.
 * Innehåller ENDAST de fält som behövs för att skapa en betalning.
 * 
 * Enligt Swish dokumentation v2.
 */
public class SwishPaymentRequest {

    private String payeeAlias; // Required: Merchant Swish number
    private String payerAlias; // Optional: Customer phone number (46712345678)
    private BigDecimal amount; // Required: Belopp (0.01 - 999999999999.99 SEK)
    private String currency; // Required: "SEK"
    private String callbackUrl; // Required: HTTPS URL för callback
    private String payeePaymentReference; // Optional: Order reference (1-35 chars)
    private String message; // Optional: Meddelande (max 50 chars)
    private String callbackIdentifier; // Optional: UUID för callback-validering (32-36 chars)
    private String payerSSN; // Optional: Personnummer
    private String ageLimit; // Optional: Åldersgräns (1-99)

    // Getters & Setters
    public String getPayeeAlias() {
        return payeeAlias;
    }

    public void setPayeeAlias(String payeeAlias) {
        this.payeeAlias = payeeAlias;
    }

    public String getPayerAlias() {
        return payerAlias;
    }

    public void setPayerAlias(String payerAlias) {
        this.payerAlias = payerAlias;
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

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getPayeePaymentReference() {
        return payeePaymentReference;
    }

    public void setPayeePaymentReference(String payeePaymentReference) {
        this.payeePaymentReference = payeePaymentReference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCallbackIdentifier() {
        return callbackIdentifier;
    }

    public void setCallbackIdentifier(String callbackIdentifier) {
        this.callbackIdentifier = callbackIdentifier;
    }

    public String getPayerSSN() {
        return payerSSN;
    }

    public void setPayerSSN(String payerSSN) {
        this.payerSSN = payerSSN;
    }

    public String getAgeLimit() {
        return ageLimit;
    }

    public void setAgeLimit(String ageLimit) {
        this.ageLimit = ageLimit;
    }
}
