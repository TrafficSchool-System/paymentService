package com.example.paymentService.features.payment.mapper;

import com.example.paymentService.features.packages.entity.Package;
import com.example.paymentService.features.payment.dto.ManualPaymentResponseDTO;
import com.example.paymentService.features.payment.dto.PaymentResponseDTO;
import com.example.paymentService.features.payment.dto.PaymentStatusDTO;
import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.features.payment.client.swish.SwishProperties;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

/**
 * ==========================================
 * PAYMENT MAPPER
 * ==========================================
 * 
 * Transforms Payment entities to DTOs for API responses.
 * 
 * RESPONSIBILITIES:
 * - Entity → DTO conversion
 * - Calculate derived fields (deep links, QR codes, expiry)
 * - Keep transformation logic centralized
 * 
 * PATTERN:
 * - Separates transformation from business logic
 * - Makes use cases cleaner
 * - Single source of truth for DTO creation
 */
@Component
public class PaymentMapper {

    private final SwishProperties swishProperties;

    public PaymentMapper(SwishProperties swishProperties) {
        this.swishProperties = swishProperties;
    }

    /**
     * Convert Payment entity to PaymentResponseDTO for Swish payment initiation.
     * 
     * Includes:
     * - Payment ID
     * - Swish deep link (for app integration)
     * - QR code data
     * - Expiry time
     */
    public PaymentResponseDTO toPaymentResponse(Payment payment) {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentId(payment.getId());

        // Swish M-Commerce: Build deep link and QR code data
        // Format: swish://paymentrequest?token=<instructionUUID>&callbackurl=<url>
        String swishDeepLink = "swish://paymentrequest?token=" + payment.getId() +
                "&callbackurl=" + swishProperties.getCallbackUrl();
        response.setSwishDeepLink(swishDeepLink);
        response.setQrCodeData(swishDeepLink);

        // Convert expiresAt from LocalDateTime to Instant
        response.setExpiresAt(payment.getExpiresAt()
                .atZone(ZoneId.systemDefault())
                .toInstant());

        return response;
    }

    /**
     * Convert Payment entity to ManualPaymentResponseDTO for admin operations.
     * 
     * Includes:
     * - Payment details
     * - Package information
     * - Status and method
     */
    public ManualPaymentResponseDTO toManualPaymentResponse(Payment payment, Package pkg) {
        return ManualPaymentResponseDTO.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .packageId(payment.getPackageId())
                .amount(payment.getAmount())
                .status(payment.getStatus().toString())
                .paymentMethod(payment.getPaymentMethod().toString())
                .packageName(pkg.getName())
                .build();
    }

    /**
     * Convert Payment entity to PaymentStatusDTO for status queries.
     * 
     * Includes:
     * - All payment details
     * - Current status
     * - Timestamps
     * - Error information (if any)
     * 
     * USED BY:
     * - GET /api/payments/{id} endpoint
     * - User payment history
     */
    public PaymentStatusDTO toPaymentStatus(Payment payment) {
        PaymentStatusDTO dto = new PaymentStatusDTO();
        dto.setId(payment.getId());
        dto.setUserId(payment.getUserId());
        dto.setPackageId(payment.getPackageId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus().toString());
        dto.setPaymentMethod(payment.getPaymentMethod().toString());
        dto.setPayerAlias(payment.getPayerAlias());
        dto.setPaymentReference(payment.getPaymentReference());
        dto.setPaidAt(payment.getPaidAt());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setExpiresAt(payment.getExpiresAt());
        dto.setErrorCode(payment.getErrorCode());
        dto.setErrorMessage(payment.getErrorMessage());
        return dto;
    }
}
