package com.example.paymentService.features.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for manual payment creation
 * Returns payment details + package information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualPaymentResponseDTO {
    private String id;
    private Long userId;
    private Long packageId;
    private BigDecimal amount;
    private String status;
    private String paymentMethod;
    private String packageName;
}
