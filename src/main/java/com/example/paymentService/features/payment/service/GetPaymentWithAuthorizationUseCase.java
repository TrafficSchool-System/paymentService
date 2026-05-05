package com.example.paymentService.features.payment.service;

import com.example.paymentService.features.payment.dto.PaymentStatusDTO;
import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.features.payment.mapper.PaymentMapper;
import com.example.paymentService.shared.security.CustomUserAuthentication;
import com.example.paymentService.shared.security.OwnershipAuthorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ==========================================
 * GET PAYMENT WITH AUTHORIZATION USE CASE
 * ==========================================
 * 
 * Fetch payment with ownership validation.
 * 
 * AUTHORIZATION:
 * - Admin/Service: Can view any payment
 * - User: Can view only their own payments
 * 
 * PATTERN:
 * - Delegates authorization to OwnershipAuthorizer
 * - Clean separation of concerns
 * - Single source of truth for authorization
 */
@Service
public class GetPaymentWithAuthorizationUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetPaymentWithAuthorizationUseCase.class);

    private final GetPaymentUseCase getPaymentUseCase;
    private final OwnershipAuthorizer ownershipAuthorizer;
    private final PaymentMapper paymentMapper;

    public GetPaymentWithAuthorizationUseCase(
            GetPaymentUseCase getPaymentUseCase,
            OwnershipAuthorizer ownershipAuthorizer,
            PaymentMapper paymentMapper) {
        this.getPaymentUseCase = getPaymentUseCase;
        this.ownershipAuthorizer = ownershipAuthorizer;
        this.paymentMapper = paymentMapper;
    }

    public PaymentStatusDTO execute(String paymentId, CustomUserAuthentication auth) {
        log.debug("Authorization check - paymentId: {}, user: {}, role: {}",
                paymentId, auth.getUserId(), auth.getRole());

        // 1. Fetch payment
        Payment payment = getPaymentUseCase.execute(paymentId);

        // 2. Authorize access
        ownershipAuthorizer.authorizePaymentAccess(auth, payment);

        log.debug("Authorization passed");

        // 3. Convert to DTO
        return paymentMapper.toPaymentStatus(payment);
    }
}
