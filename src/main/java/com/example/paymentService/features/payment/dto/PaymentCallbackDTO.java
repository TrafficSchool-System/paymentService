package com.example.paymentService.features.payment.dto;

import com.example.paymentService.features.payment.entity.PaymentStatus;

/**
 * Data Transfer Object (DTO) för callback från betalningsleverantör.
 *
 * Används för att ta emot information från systemet när statusen
 * för en betalning ändras, t.ex. när Swish skickar en callback
 * efter att en betalning har genomförts eller misslyckats.
 *
 * Innehåller:
 * - paymentId: Unikt ID för betalningen (instructionUUID)
 * - callbackIdentifier: Identifierare som används för att spåra callback
 * - status: Aktuell status för betalningen (PaymentStatus)
 * - errorMessage: Felmeddelande om betalningen misslyckades
 *
 * DTO:n innehåller ingen affärslogik och används enbart för dataöverföring.
 */

public class PaymentCallbackDTO {

    private String paymentId;
    private String callbackIdentifier;
    private PaymentStatus status;
    private String errorMessage;

    // Getters & Setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getCallbackIdentifier() { return callbackIdentifier; }
    public void setCallbackIdentifier(String callbackIdentifier) { this.callbackIdentifier = callbackIdentifier; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

}
