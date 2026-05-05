package com.example.paymentService.features.payment.integration;

import com.example.paymentService.features.payment.client.swish.SwishClient;
import com.example.paymentService.features.payment.client.swish.dto.SwishPaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * ==========================================
 * SWISH PAYMENT GATEWAY IMPLEMENTATION
 * ==========================================
 * 
 * Implementation of SwishPaymentGateway using SwishClient.
 * 
 * RESPONSIBILITIES:
 * - Delegate to existing SwishClient
 * - Provide clean abstraction for use cases
 * - Isolate external dependency
 * 
 * BENEFITS:
 * - Use cases don't depend directly on SwishClient
 * - Easy to mock in tests
 * - Can switch payment providers without changing use cases
 * - Follows Dependency Inversion Principle
 */
@Component
public class SwishPaymentGatewayImpl implements SwishPaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(SwishPaymentGatewayImpl.class);

    private final SwishClient swishClient;

    public SwishPaymentGatewayImpl(SwishClient swishClient) {
        this.swishClient = swishClient;
    }

    @Override
    public ResponseEntity<String> createPayment(String paymentId, SwishPaymentRequest swishRequest) {
        log.debug("Gateway: Delegating payment creation to SwishClient for payment: {}", paymentId);

        // Delegate to existing SwishClient
        ResponseEntity<String> response = swishClient.createPayment(paymentId, swishRequest);

        log.debug("Gateway: Swish client returned status: {}", response.getStatusCode());
        return response;
    }
}
