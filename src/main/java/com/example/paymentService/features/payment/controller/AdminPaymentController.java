package com.example.paymentService.features.payment.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.paymentService.features.payment.dto.CreateManualPaymentDTO;
import com.example.paymentService.features.payment.dto.ManualPaymentResponseDTO;
import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.features.payment.service.CreateManualPaymentWithResponseUseCase;
import com.example.paymentService.features.payment.service.DeleteUserPaymentsUseCase;
import com.example.paymentService.features.payment.service.GetAllPaymentsUseCase;
import com.example.paymentService.features.payment.service.GetUserPaymentsUseCase;

import jakarta.validation.Valid;

/**
 * ==========================================
 * ADMIN PAYMENT CONTROLLER
 * ==========================================
 * 
 * RESTful endpoints for admin payment management.
 * Base path: /api/admin/payments
 * 
 * ENDPOINTS:
 * - GET /admin/payments : List all payments
 * - GET /admin/users/{userId}/payments : List payments for user
 * - POST /admin/payments/manual : Create manual payment
 * - DELETE /admin/payments/users/{userId} : Delete user payments
 * 
 * CLEAN ARCHITECTURE:
 * - HTTP layer only (request/response handling)
 * - NO business logic
 * - Delegates to use cases
 * 
 * SECURITY:
 * - Admin or internal service only
 * - Used by AdminService for user management
 * - Used by admin dashboard for monitoring
 */
@RestController
@RequestMapping("/api/admin/payments")
@PreAuthorize("hasAnyRole('ADMIN', 'INTERNAL_SERVICE')")
public class AdminPaymentController {

    private static final Logger log = LoggerFactory.getLogger(AdminPaymentController.class);

    private final GetAllPaymentsUseCase getAllPaymentsUseCase;
    private final GetUserPaymentsUseCase getUserPaymentsUseCase;
    private final CreateManualPaymentWithResponseUseCase createManualPaymentWithResponseUseCase;
    private final DeleteUserPaymentsUseCase deleteUserPaymentsUseCase;

    public AdminPaymentController(
            GetAllPaymentsUseCase getAllPaymentsUseCase,
            GetUserPaymentsUseCase getUserPaymentsUseCase,
            CreateManualPaymentWithResponseUseCase createManualPaymentWithResponseUseCase,
            DeleteUserPaymentsUseCase deleteUserPaymentsUseCase) {
        this.getAllPaymentsUseCase = getAllPaymentsUseCase;
        this.getUserPaymentsUseCase = getUserPaymentsUseCase;
        this.createManualPaymentWithResponseUseCase = createManualPaymentWithResponseUseCase;
        this.deleteUserPaymentsUseCase = deleteUserPaymentsUseCase;
    }

    /**
     * LIST ALL PAYMENTS
     * GET /api/admin/payments
     * 
     * Returns all payments for admin monitoring.
     * 
     * @return List of all payments
     */
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        log.info("📋 GET /api/admin/payments - Admin fetching all payments");

        List<Payment> payments = getAllPaymentsUseCase.execute();

        log.info("✅ Found {} payments", payments.size());
        return ResponseEntity.ok(payments);
    }

    /**
     * GET USER PAYMENTS
     * GET /api/admin/users/{userId}/payments
     * 
     * Returns all payments for a specific user.
     * 
     * @param userId User ID to get payments for
     * @return List of payments for the user
     */
    @GetMapping("/users/{userId}/payments")
    public ResponseEntity<List<Payment>> getUserPayments(@PathVariable Long userId) {
        log.info("📋 GET /api/admin/users/{}/payments - Admin fetching user payments", userId);

        List<Payment> payments = getUserPaymentsUseCase.execute(userId);

        log.info("✅ Found {} payments for user {}", payments.size(), userId);
        return ResponseEntity.ok(payments);
    }

    /**
     * CREATE MANUAL PAYMENT
     * POST /api/admin/payments/manual
     * 
     * Creates a manual payment (no Swish integration).
     * Payment is immediately marked as PAID with MANUAL method.
     * 
     * USED WHEN:
     * - Admin creates user + subscription manually
     * - Payment received in person at traffic school
     * 
     * @param request Manual payment details (userId, packageId)
     * @return Created payment with package details
     */
    @PostMapping("/manual")
    public ResponseEntity<ManualPaymentResponseDTO> createManualPayment(
            @Valid @RequestBody CreateManualPaymentDTO request) {

        log.info("🎫 POST /api/admin/payments/manual - Creating manual payment for user {}, package {}",
                request.getUserId(), request.getPackageId());

        ManualPaymentResponseDTO payment = createManualPaymentWithResponseUseCase.execute(request);

        log.info("✅ Manual payment created: {}", payment.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    /**
     * DELETE USER PAYMENTS (CASCADE DELETE)
     * DELETE /api/admin/payments/users/{userId}
     * 
     * Deletes all payments for a specific user.
     * Used during cascade delete when UserService deletes a user.
     * 
     * SECURITY:
     * - Only available to ADMIN or INTERNAL_SERVICE
     * - Called automatically from UserService on user deletion
     * 
     * @param userId User ID whose payments will be deleted
     * @return Response with deletion count
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUserPayments(@PathVariable Long userId) {
        log.info("🗑️ DELETE /api/admin/payments/users/{} - Cascade delete payments", userId);

        // Get count before deletion (for response message)
        List<Payment> payments = getUserPaymentsUseCase.execute(userId);
        int count = payments.size();

        if (count > 0) {
            deleteUserPaymentsUseCase.execute(userId);
            log.info("✅ Deleted {} payments for user {}", count, userId);
            return ResponseEntity.ok("Deleted " + count + " payments for user " + userId);
        } else {
            log.info("ℹ️ No payments found for user {}", userId);
            return ResponseEntity.ok("No payments found for user " + userId);
        }
    }
}
