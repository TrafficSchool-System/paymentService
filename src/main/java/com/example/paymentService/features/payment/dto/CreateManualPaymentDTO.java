package com.example.paymentService.features.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin creating manual payment (no Swish integration)
 * 
 * Used when admin creates user + subscription manually (e.g., sold in person).
 * Payment is immediately marked as PAID with MANUAL method.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateManualPaymentDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Package ID is required")
    private Long packageId;

    private String note; // Optional admin note (e.g., "Sold in person at traffic school")
}
