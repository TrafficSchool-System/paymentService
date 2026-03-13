package com.example.paymentService.Controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.paymentService.Entity.Payment;
import com.example.paymentService.swish.Service.PaymentService;

/**
 * ADMIN PAYMENT CONTROLLER
 * 
 * RESTful endpoints for admin payment management operations.
 * Base path: /api/admin/payments
 * 
 * ADMIN OPERATIONS:
 * - GET /admin/payments : List all payments in system
 * - GET /admin/users/{userId}/payments : List payments for specific user
 * 
 * AUTHENTICATION:
 * - All endpoints require ADMIN or INTERNAL_SERVICE role
 * - ADMIN: Via JWT token from API Gateway
 * - INTERNAL_SERVICE: Via X-Internal-API-Key from other microservices (e.g.,
 * AdminService)
 * 
 * DESIGN PATTERN:
 * - Admin endpoints separated from user endpoints
 * - Focus on monitoring and oversight
 * - Used by admin dashboard for payment analytics
 */
@RestController
@RequestMapping("/api/admin/payments")
@PreAuthorize("hasAnyRole('ADMIN', 'INTERNAL_SERVICE')")
public class AdminPaymentController {

    private static final Logger log = LoggerFactory.getLogger(AdminPaymentController.class);

    private final PaymentService paymentService;

    public AdminPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * LIST ALL PAYMENTS
     * GET /api/admin/payments
     * 
     * Returns all payments in the system for admin monitoring.
     * Used by admin dashboard to display payment analytics and history.
     * 
     * @return List of all payments
     */
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        log.info("📋 GET /api/admin/payments - Admin fetching all payments");

        List<Payment> payments = paymentService.getAllPayments();
        log.info("✅ Found {} payments", payments.size());

        return ResponseEntity.ok(payments);
    }

    /**
     * GET USER PAYMENTS
     * GET /api/admin/users/{userId}/payments
     * 
     * Returns all payments for a specific user.
     * Used by admin to monitor user payment history.
     * 
     * @param userId User ID to get payments for
     * @return List of payments for the user
     */
    @GetMapping("/users/{userId}/payments")
    public ResponseEntity<List<Payment>> getUserPayments(@PathVariable Long userId) {
        log.info("📋 GET /api/admin/users/{}/payments - Admin fetching user payments", userId);

        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        log.info("✅ Found {} payments for user {}", payments.size(), userId);

        return ResponseEntity.ok(payments);
    }

    /**
     * DELETE USER PAYMENTS (CASCADE DELETE)
     * DELETE /api/admin/payments/users/{userId}
     * 
     * Raderar alla betalningar för en specifik användare.
     * Används vid cascade delete när UserService raderar en användare.
     * 
     * SÄKERHET:
     * - Endast tillgänglig för ADMIN eller INTERNAL_SERVICE
     * - Anropas automatiskt från UserService vid användarradering
     * 
     * @param userId User ID vars betalningar ska raderas
     * @return ResponseEntity med antal raderade betalningar
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUserPayments(@PathVariable Long userId){
        log.info("🗑️ DELETE /api/admin/payments/users/{} - Cascade delete payments", userId);

        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        int count = payments.size(); 

        if (count > 0) {
            paymentService.deletePaymentsByUserId(userId); 
            log.info("✅ Deleted {} payments for user {}", count, userId);
            return ResponseEntity.ok("Deleted " + count + " payments for user " + userId); 
        } else {
            log.info("ℹ️ No payments found for user {}", userId);
            return ResponseEntity.ok("No payments found for user " + userId); 
        }
    }
}
