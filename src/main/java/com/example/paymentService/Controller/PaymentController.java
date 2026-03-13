package com.example.paymentService.Controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.paymentService.Dto.CreatePaymentRequestDTO;
import com.example.paymentService.Dto.PaymentResponseDTO;
import com.example.paymentService.Entity.Payment;
import com.example.paymentService.swish.Service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * PAYMENT CONTROLLER
 * 
 * RESTful endpoints for user payment operations.
 * Base path: /api/payments
 * 
 * USER OPERATIONS:
 * - POST /payments : Create new payment (initiate Swish payment)
 * - GET /payments/{id} : Get payment status (with authorization)
 * 
 * ADMIN OPERATIONS:
 * - See AdminPaymentController for admin payment management
 * 
 * AUTHENTICATION:
 * - All endpoints require authentication
 * - Authorization checked in service layer for payment access
 * - Uses X-User-Id header from API Gateway
 */
@RestController
@RequestMapping("/api/payments")
@PreAuthorize("isAuthenticated()")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Hämtar userId från JWT token.
     * Samma mönster som ExamController använder.
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    /**
     * CREATE NEW PAYMENT
     * POST /api/payments
     * 
     * Initiates a new Swish payment for a package.
     * User ID is extracted from Gateway headers.
     * 
     * @param request     Payment details (payerAlias, packageId)
     * @param httpRequest HTTP request with user context
     * @return Payment response with payment ID
     */
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @RequestBody CreatePaymentRequestDTO request,
            HttpServletRequest httpRequest) {

        // Extract userId from JWT (set by Gateway)
        Long userId = (Long) httpRequest.getAttribute("userId");
        request.setUserId(userId);

        log.info("🚀 POST /api/payments - userId: {}, packageId: {}",
                userId, request.getPackageId());

        PaymentResponseDTO response = paymentService.initiatePayment(request);
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
     * @param paymentId Payment ID to retrieve
     * @param request   HTTP request with user context
     * @return Payment details
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentStatus(
            @PathVariable String paymentId,
            HttpServletRequest request) {

        // Extract userId from JWT
        Long userId = getUserIdFromRequest(request);

        // Check if user is admin
        @SuppressWarnings("unchecked")
        List<String> authorities = (List<String>) request.getAttribute("authorities");
        boolean isAdmin = authorities != null && authorities.contains("ROLE_ADMIN");

        log.info("🔍 GET /api/payments/{} - userId: {}, isAdmin: {}",
                paymentId, userId, isAdmin);

        // Service handles authorization and throws ForbiddenException if needed
        Payment payment = paymentService.getPaymentByIdWithAuthorization(paymentId, userId, isAdmin);
        log.info("✅ Payment found: Status={}", payment.getStatus());

        return ResponseEntity.ok(payment);
    }

}
