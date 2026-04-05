package com.example.paymentService.scheduler;

import com.example.paymentService.Entity.Payment;
import com.example.paymentService.Entity.PaymentStatus;
import com.example.paymentService.Repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled Task för att automatiskt markera utgångna betalningar.
 * 
 * Körs varje minut och kollar efter PENDING/CREATED betalningar som har
 * passerat sin utgångstid.
 * Uppdaterar dessa till EXPIRED status.
 * 
 * Detta förhindrar att betalningar hänger i PENDING läge för alltid.
 * 
 * SCHEMA:
 * - Körs var 60:e sekund (fixedRate = 60000)
 * - Initial delay = 60 sekunder (väntar 1 minut efter start)
 * 
 * CRITERIA FÖR UTGÅNG:
 * - Status = PENDING eller CREATED
 * - expiresAt < LocalDateTime.now()
 */
@Component
public class PaymentExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentExpiryScheduler.class);

    private final PaymentRepository paymentRepository;

    public PaymentExpiryScheduler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Scheduled task som körs varje minut.
     * 
     * Hittar alla betalningar som:
     * 1. Har status PENDING eller CREATED
     * 2. Har passerat sin utgångstid (expiresAt < now)
     * 
     * Uppdaterar dessa till EXPIRED status.
     */
    @Scheduled(fixedRate = 60000, initialDelay = 60000) // Kör varje minut
    @Transactional
    public void expireOldPayments() {
        LocalDateTime now = LocalDateTime.now();

        log.debug("⏰ Running payment expiry check at {}", now);

        // Hitta PENDING betalningar som har gått ut
        List<Payment> pendingExpired = paymentRepository.findByStatusAndExpiresAtBefore(
                PaymentStatus.PENDING, now);

        // Hitta CREATED betalningar som har gått ut
        List<Payment> createdExpired = paymentRepository.findByStatusAndExpiresAtBefore(
                PaymentStatus.CREATED, now);

        int totalExpired = pendingExpired.size() + createdExpired.size();

        if (totalExpired > 0) {
            log.info("🔍 Found {} expired payments to update", totalExpired);

            // Uppdatera PENDING betalningar
            for (Payment payment : pendingExpired) {
                log.info("⏳ Expiring PENDING payment: {} (expired at: {})",
                        payment.getId(), payment.getExpiresAt());
                payment.setStatus(PaymentStatus.EXPIRED);
                payment.setErrorMessage("Payment expired - timeout exceeded");
                paymentRepository.save(payment);
            }

            // Uppdatera CREATED betalningar
            for (Payment payment : createdExpired) {
                log.info("⏳ Expiring CREATED payment: {} (expired at: {})",
                        payment.getId(), payment.getExpiresAt());
                payment.setStatus(PaymentStatus.EXPIRED);
                payment.setErrorMessage("Payment expired - no Swish response received");
                paymentRepository.save(payment);
            }

            log.info("✅ Successfully expired {} payments", totalExpired);
        } else {
            log.debug("✓ No expired payments found");
        }
    }
}
