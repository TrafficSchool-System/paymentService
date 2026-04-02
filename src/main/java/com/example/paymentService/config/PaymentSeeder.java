package com.example.paymentService.config;

import com.example.paymentService.Entity.Package;
import com.example.paymentService.Entity.PackageType;
import com.example.paymentService.Entity.Payment;
import com.example.paymentService.Entity.PaymentStatus;
import com.example.paymentService.Repository.PackageRepository;
import com.example.paymentService.Repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Seeder för att skapa PAID payments för test-användare.
 * 
 * Körs automatiskt vid applikationsstart och ger test-användare
 * aktiv 1-månads prenumeration.
 */
@Component
@Order(2) // Körs efter PackageSeeder
@RequiredArgsConstructor
@Slf4j
public class PaymentSeeder implements CommandLineRunner {

    private final PaymentRepository paymentRepository;
    private final PackageRepository packageRepository;

    @Override
    public void run(String... args) {
        // Hämta 1 Month package
        Package monthPackage = packageRepository.findByPackageType(PackageType.MONTH)
                .orElseThrow(() -> new RuntimeException("MONTH package not found - run PackageSeeder first!"));

        // Skapa PAID payments för test-användare (ID 1, 2 från UserSeeder)
        createPaymentIfNotExists(1L, monthPackage, "0701234567"); // Robert@transportteori.se
        createPaymentIfNotExists(2L, monthPackage, "0709876543"); // Fk@excetra.se

        log.info("✅ Payment seeding completed - {} payments in database",
                paymentRepository.count());
    }

    private void createPaymentIfNotExists(Long userId, Package pkg, String phoneNumber) {
        // Kolla om användaren redan har en aktiv betalning
        if (paymentRepository.findByUserIdAndStatus(userId, PaymentStatus.PAID).isEmpty()) {
            String paymentId = UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 32);
            String callbackId = UUID.randomUUID().toString();

            Payment payment = Payment.builder()
                    .id(paymentId)
                    .userId(userId)
                    .packageId(pkg.getId())
                    .payerAlias("46" + phoneNumber.substring(1)) // 0701234567 -> 46701234567
                    .amount(pkg.getPrice())
                    .status(PaymentStatus.PAID)
                    .paymentReference("TEST-REF-" + userId)
                    .callbackIdentifier(callbackId)
                    .createdAt(LocalDateTime.now())
                    .paidAt(LocalDateTime.now())
                    .build();

            paymentRepository.save(payment);
            log.info("💳 Created PAID payment for userId: {} - Package: {} ({}kr, 30 days)",
                    userId, pkg.getName(), pkg.getPrice());
        }
    }
}
