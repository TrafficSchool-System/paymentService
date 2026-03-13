package com.example.paymentService.Dto;

/**
 * Data Transfer Object (DTO) för att skapa en ny betalning.
 *
 * Används som request-objekt när en användare initierar en betalning
 * via API:et eller service-lagret.
 *
 * Innehåller:
 * - userId: ID för användaren som gör betalningen
 * - payerAlias: Betalarens mobilnummer i internationellt format (t.ex. 46712345678)
 * - packageId: ID för paketet som användaren vill köpa
 */

public class CreatePaymentRequestDTO {

    private Long userId;     // Användar-ID
    private String payerAlias; // Mobilnummer
    private Long packageId;    // Paketet användaren vill köpa

    // Getters & Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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