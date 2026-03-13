package com.example.paymentService.Dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) för respons efter skapad betalning.
 *
 * Används för att skicka tillbaka information till klienten eller front-end
 * efter att en betalning har initierats, t.ex. via Swish.
 *
 * Innehåller:
 * - paymentId: Unikt ID för betalningen (instructionUUID)
 * - swishDeepLink: Deep link som öppnar Swish-appen
 * - qrCodeData: Data för QR-kod som användaren kan scanna
 * - expiresAt: När betalningen går ut
 *
 * Detta objekt innehåller ingen affärslogik och används enbart för
 * dataöverföring.
 */

public class PaymentResponseDTO {

    private String paymentId; // instructionUUID
    private String swishDeepLink; // URL som öppnar Swish-appen
    private String qrCodeData; // Data för QR-kod
    private LocalDateTime expiresAt; // När betalningen går ut

    // Getters & Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getSwishDeepLink() {
        return swishDeepLink;
    }

    public void setSwishDeepLink(String swishDeepLink) {
        this.swishDeepLink = swishDeepLink;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

}
