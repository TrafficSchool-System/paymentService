package com.example.paymentService.shared.security;

import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.shared.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ==========================================
 * OWNERSHIP AUTHORIZER
 * ==========================================
 * 
 * Centralized authorization logic for ownership validation.
 * 
 * RESPONSIBILITIES:
 * - Verify user can access their own resources
 * - Allow admin/service access to all resources
 * - Throw ForbiddenException on unauthorized access
 * 
 * PATTERN:
 * - Used by use cases (NOT controllers)
 * - Single source of truth for authorization
 * - Clean separation of concerns
 * 
 * EXAMPLE USAGE:
 * ```java
 * 
 * @Service
 *          public class GetPaymentUseCase {
 *          private final OwnershipAuthorizer authorizer;
 * 
 *          public Payment execute(String paymentId, CustomUserAuthentication
 *          auth) {
 *          Payment payment = paymentRepository.findById(paymentId)...;
 *          authorizer.authorizePaymentAccess(auth, payment);
 *          return payment;
 *          }
 *          }
 *          ```
 */
@Service
public class OwnershipAuthorizer {

    private static final Logger log = LoggerFactory.getLogger(OwnershipAuthorizer.class);

    /**
     * Authorize user access to a specific user's data.
     * 
     * ALLOWS:
     * - Admin can access any user
     * - Internal service can access any user
     * - User can access their own data
     * 
     * DENIES:
     * - User accessing other user's data
     * 
     * @param auth         Authenticated user
     * @param targetUserId User ID being accessed
     * @throws ForbiddenException if unauthorized
     */
    public void authorizeUserAccess(CustomUserAuthentication auth, Long targetUserId) {
        // Admin and services have full access
        if (auth.hasAdminAccess()) {
            log.debug("✅ Admin/Service access granted for userId: {}", targetUserId);
            return;
        }

        // User can only access their own data
        if (!auth.getUserId().equals(targetUserId)) {
            log.warn("❌ FORBIDDEN: User {} tried to access userId {}",
                    auth.getUserId(), targetUserId);
            throw new ForbiddenException(
                    "You do not have permission to access this user's data");
        }

        log.debug("✅ User access granted for own data");
    }

    /**
     * Authorize payment access.
     * 
     * ALLOWS:
     * - Admin can view any payment
     * - Internal service can view any payment
     * - User can view their own payments
     * 
     * DENIES:
     * - User viewing other user's payments
     * 
     * @param auth    Authenticated user
     * @param payment Payment being accessed
     * @throws ForbiddenException if unauthorized
     */
    public void authorizePaymentAccess(CustomUserAuthentication auth, Payment payment) {
        // Admin and services have full access
        if (auth.hasAdminAccess()) {
            log.debug("✅ Admin/Service access granted for payment: {}", payment.getId());
            return;
        }

        // User can only access their own payments
        if (!auth.getUserId().equals(payment.getUserId())) {
            log.warn("❌ FORBIDDEN: User {} tried to access payment {} owned by user {}",
                    auth.getUserId(), payment.getId(), payment.getUserId());
            throw new ForbiddenException(
                    "You do not have permission to view this payment");
        }

        log.debug("✅ User access granted for own payment");
    }
}
