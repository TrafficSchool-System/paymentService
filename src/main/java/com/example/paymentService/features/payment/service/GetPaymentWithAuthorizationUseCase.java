package com.example.paymentService.features.payment.service;

import com.example.paymentService.features.payment.client.swish.SwishClient;
import com.example.paymentService.features.payment.client.swish.dto.SwishPaymentResponse;
import com.example.paymentService.features.payment.dto.PaymentStatusDTO;
import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.features.payment.entity.PaymentStatus;
import com.example.paymentService.features.payment.mapper.PaymentMapper;
import com.example.paymentService.features.payment.repository.PaymentRepository;
import com.example.paymentService.shared.security.CustomUserAuthentication;
import com.example.paymentService.shared.security.OwnershipAuthorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
    private final SwishClient swishClient;
    private final HandleSwishCallbackUseCase handleSwishCallbackUseCase;
    private final PaymentRepository paymentRepository;

    public GetPaymentWithAuthorizationUseCase(
            GetPaymentUseCase getPaymentUseCase,
            OwnershipAuthorizer ownershipAuthorizer,
            PaymentMapper paymentMapper,
            SwishClient swishClient,
            HandleSwishCallbackUseCase handleSwishCallbackUseCase,
            PaymentRepository paymentRepository) {
        this.getPaymentUseCase = getPaymentUseCase;
        this.ownershipAuthorizer = ownershipAuthorizer;
        this.paymentMapper = paymentMapper;
        this.swishClient = swishClient;
        this.handleSwishCallbackUseCase = handleSwishCallbackUseCase;
        this.paymentRepository = paymentRepository;
    }

    public PaymentStatusDTO execute(String paymentId, CustomUserAuthentication auth) {
        log.debug("Authorization check - paymentId: {}, user: {}, role: {}",
                paymentId, auth.getUserId(), auth.getRole());

        // 1. Fetch payment from DB
        Payment payment = getPaymentUseCase.execute(paymentId);

        // 2. Authorize access
        ownershipAuthorizer.authorizePaymentAccess(auth, payment);

        // 3. If still pending, ask Swish directly as webhook fallback
        if (payment.getStatus() == PaymentStatus.CREATED || payment.getStatus() == PaymentStatus.PENDING) {
            syncStatusFromSwish(payment);
            // Re-fetch updated payment after potential sync
            payment = getPaymentUseCase.execute(paymentId);
        }

        log.debug("Authorization passed, status: {}", payment.getStatus());

        // 4. Convert to DTO
        return paymentMapper.toPaymentStatus(payment);
    }

    /**
     * Fråga Swish direkt om betalningstatus och uppdatera DB om statusen har
     * förändrats. Detta är ett fallback-mönster för när webhook inte levererades.
     */
    private void syncStatusFromSwish(Payment payment) {
        try {
            ResponseEntity<SwishPaymentResponse> response = swishClient.getPaymentStatus(payment.getId());
            SwishPaymentResponse swishStatus = response.getBody();

            if (swishStatus == null || swishStatus.getStatus() == null) {
                return;
            }

            String swishStatusStr = swishStatus.getStatus();
            log.debug("Swish status for {}: {}", payment.getId(), swishStatusStr);

            // Om Swish säger PAID men DB säger PENDING → trigga callback-logiken
            if ("PAID".equals(swishStatusStr) && payment.getStatus() != PaymentStatus.PAID) {
                log.info("Swish says PAID but DB says {} — syncing via callback handler", payment.getStatus());
                handleSwishCallbackUseCase.execute(swishStatus);
            }
        } catch (Exception e) {
            // Logga men krascha inte — returnera DB-status som fallback
            log.warn("Could not sync status from Swish for payment {}: {}", payment.getId(), e.getMessage());
        }
    }
}
