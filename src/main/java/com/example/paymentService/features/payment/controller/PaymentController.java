package com.example.paymentService.features.payment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.paymentService.features.payment.dto.CreatePaymentRequestDTO;
import com.example.paymentService.features.payment.dto.PaymentResponseDTO;
import com.example.paymentService.features.payment.dto.PaymentStatusDTO;
import com.example.paymentService.features.payment.service.GetPaymentWithAuthorizationUseCase;
import com.example.paymentService.features.payment.service.InitiatePaymentUseCase;
import com.example.paymentService.shared.security.CustomUserAuthentication;

/**
 * ==========================================
 * PAYMENT CONTROLLER
 * ==========================================
 * 
 * RESTful endpoints for user payment operations.
 * Base path: /api/payments
 * 
 * ENDPOINTS:
 * - POST /payments : Create new payment (initiate Swish payment)
 * - GET /payments/{id} : Get payment status (with authorization)
 * 
 * CLEAN ARCHITECTURE:
 * - HTTP layer only (request/response handling)
 * - NO business logic
 * - Delegates to use cases
 * - Uses @AuthenticationPrincipal for user context
 * 
 * SECURITY:
 * - Gateway-first authentication
 * - CustomUserAuthentication from SecurityContext
 * - Authorization delegated to use cases
 */
@RestController
@RequestMapping("/api/payments")
@PreAuthorize("isAuthenticated()")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final InitiatePaymentUseCase initiatePaymentUseCase;
    private final GetPaymentWithAuthorizationUseCase getPaymentWithAuthorizationUseCase;

    public PaymentController(
            InitiatePaymentUseCase initiatePaymentUseCase,
            GetPaymentWithAuthorizationUseCase getPaymentWithAuthorizationUseCase) {
        this.initiatePaymentUseCase = initiatePaymentUseCase;
        this.getPaymentWithAuthorizationUseCase = getPaymentWithAuthorizationUseCase;
    }

    /**
     * CREATE NEW PAYMENT
     * POST /api/payments
     * 
     * Initiates a new Swish payment for a package.
     * User ID is extracted from authentication context (SECURE).
     * 
     * SECURITY:
     * - userId comes from JWT token (via @AuthenticationPrincipal)
     * - userId is NOT accepted from request body (prevents spoofing)
     * - userId is passed directly to use case
     * 
     * @param request Payment details (payerAlias, packageId) - NO userId
     * @param auth    Authenticated user from SecurityContext
     * @return Payment response with payment ID and deep link
     */
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @RequestBody CreatePaymentRequestDTO request,
            @AuthenticationPrincipal CustomUserAuthentication auth) {

        log.info("🚀 POST /api/payments - userId: {}, packageId: {}",
                auth.getUserId(), request.getPackageId());

        // Delegate to use case with userId from authentication
        PaymentResponseDTO response = initiatePaymentUseCase.execute(request, auth.getUserId());

        log.info("✅ Payment created: {}", response.getPaymentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET PAYMENT STATUS
     * GET /api/payments/{paymentId}
     * 
     * Returns payment status with authorization check.
     * Users can only view their own payments, admins can view all.
     * 
     * CLEAN ARCHITECTURE:
     * - Returns DTO (not entity)
     * - Authorization handled in use case
     * - DTO mapping handled by PaymentMapper
     * 
     * @param paymentId Payment ID to retrieve
     * @param auth      Authenticated user from SecurityContext
     * @return Payment details as DTO
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentStatusDTO> getPaymentStatus(
            @PathVariable String paymentId,
            @AuthenticationPrincipal CustomUserAuthentication auth) {

        log.info("🔍 GET /api/payments/{} - userId: {}, role: {}",
                paymentId, auth.getUserId(), auth.getRole());

        // Delegate to use case (handles authorization + mapping)
        PaymentStatusDTO payment = getPaymentWithAuthorizationUseCase.execute(paymentId, auth);

        log.info("✅ Payment found: Status={}", payment.getStatus());
        return ResponseEntity.ok(payment);
    }
}
