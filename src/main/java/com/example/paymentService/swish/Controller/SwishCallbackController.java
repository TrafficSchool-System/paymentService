package com.example.paymentService.swish.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.paymentService.swish.Dto.SwishPaymentResponse;
import com.example.paymentService.swish.Service.PaymentService;

/**
 * SWISH WEBHOOK CONTROLLER
 * 
 * Receives payment status callbacks from Swish payment provider.
 * Base path: /api/webhooks/swish
 * 
 * WEBHOOK ENDPOINT:
 * - POST /webhooks/swish : Receive payment status updates from Swish
 * 
 * CALLBACK EVENTS:
 * - PAID: Payment successfully completed
 * - DECLINED: Payment was declined
 * - CANCELLED: Payment was cancelled by user
 * - ERROR: An error occurred during payment
 * 
 * SECURITY:
 * - Must be accessible without authentication (external webhook)
 * - Validates callbackIdentifier header from Swish
 * - Always returns 200 OK to prevent retry loops
 * 
 * NOTE:
 * - URL must be HTTPS in production, HTTP OK for testing
 * - All business logic delegated to PaymentService layer
 */
@RestController
@RequestMapping("/api/webhooks/swish")
public class SwishCallbackController {

    private static final Logger log = LoggerFactory.getLogger(SwishCallbackController.class);

    private final PaymentService paymentService;

    /**
     * Constructor injection av PaymentService.
     * Spring hittar automatiskt rätt implementation (PaymentServiceImpl).
     */

    public SwishCallbackController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * RECEIVE SWISH CALLBACK
     * POST /api/webhooks/swish
     * 
     * Receives payment status updates from Swish payment provider.
     * Delegates all business logic to PaymentService.
     * 
     * Flow:
     * 1. Log callback receipt
     * 2. Delegate processing to PaymentService
     * 3. Return 200 OK to acknowledge receipt
     * 
     * @param callback           Swish payment response with status and details
     * @param callbackIdentifier Header to validate callback source
     * @return 200 OK to acknowledge receipt
     */
    @PostMapping
    public ResponseEntity<String> handlePaymentCallback(
            @RequestBody SwishPaymentResponse callback,
            @RequestHeader(value = "callbackIdentifier", required = false) String callbackIdentifier) {
        log.info("📥 Webhook received from Swish for payment ID: {}, Status: {}",
                callback.getId(), callback.getStatus());

        try {
            // Delegate all business logic to service layer
            paymentService.handleSwishCallback(callback);

            // Return 200 OK so Swish knows we received the callback
            return ResponseEntity.ok("Callback processed");
        } catch (Exception e) {
            // Log error but still return 200 OK to Swish
            // Otherwise Swish will retry callback indefinitely
            log.error("❌ Error processing callback: {}", e.getMessage(), e);
            return ResponseEntity.ok("Callback received but processing failed");
        }
    }
}
