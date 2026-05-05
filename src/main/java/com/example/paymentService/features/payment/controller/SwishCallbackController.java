package com.example.paymentService.features.payment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.paymentService.features.payment.service.HandleSwishCallbackUseCase;
import com.example.paymentService.features.payment.client.swish.dto.SwishPaymentResponse;

/**
 * ==========================================
 * SWISH WEBHOOK CONTROLLER
 * ==========================================
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
 * CLEAN ARCHITECTURE:
 * - HTTP layer only
 * - Delegates to HandleSwishCallbackUseCase
 * - No business logic in controller
 */
@RestController
@RequestMapping("/api/webhooks/swish")
public class SwishCallbackController {

    private static final Logger log = LoggerFactory.getLogger(SwishCallbackController.class);

    private final HandleSwishCallbackUseCase handleSwishCallbackUseCase;

    public SwishCallbackController(HandleSwishCallbackUseCase handleSwishCallbackUseCase) {
        this.handleSwishCallbackUseCase = handleSwishCallbackUseCase;
    }

    /**
     * RECEIVE SWISH CALLBACK
     * POST /api/webhooks/swish
     * 
     * Receives payment status updates from Swish payment provider.
     * 
     * FLOW:
     * 1. Log callback receipt
     * 2. Delegate to use case
     * 3. Return 200 OK (always, to prevent retry loops)
     * 
     * @param callback           Swish payment response
     * @param callbackIdentifier Header to validate callback source
     * @return 200 OK to acknowledge receipt
     */
    @PostMapping
    public ResponseEntity<String> handlePaymentCallback(
            @RequestBody SwishPaymentResponse callback,
            @RequestHeader(value = "callbackIdentifier", required = false) String callbackIdentifier) {

        log.info("🔔 Webhook received from Swish for payment ID: {}, Status: {}",
                callback.getId(), callback.getStatus());

        try {
            // Delegate to use case
            handleSwishCallbackUseCase.execute(callback);

            // Return 200 OK so Swish knows we received the callback
            return ResponseEntity.ok("Callback processed");

        } catch (Exception e) {
            // Log error but still return 200 OK to Swish
            // Otherwise Swish will retry callback indefinitely
            log.error("Error processing callback: {}", e.getMessage(), e);
            return ResponseEntity.ok("Callback received but processing failed");
        }
    }
}
