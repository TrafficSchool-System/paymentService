package com.example.paymentService.config;

import com.example.paymentService.Entity.Package;
import com.example.paymentService.Entity.PackageType;
import com.example.paymentService.Repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeder för att skapa standard packages i systemet.
 * 
 * Körs automatiskt vid applikationsstart och skapar
 * tre standard-prenumerationspaket om de inte redan finns.
 */
@Component
@Order(1) // Körs först (innan PaymentSeeder)
@RequiredArgsConstructor
@Slf4j
public class PackageSeeder implements CommandLineRunner {

    private final PackageRepository packageRepository;

    @Override
    public void run(String... args) {
        createPackageIfNotExists(
                PackageType.DAY,
                "1 Day Access",
                new BigDecimal("99.00"),
                "24 hours full access to all quizzes and exams",
                1,
                24);

        createPackageIfNotExists(
                PackageType.WEEK,
                "1 Week Access",
                new BigDecimal("349.00"),
                "7 days full access to all quizzes and exams",
                7,
                168);

        createPackageIfNotExists(
                PackageType.MONTH,
                "1 Month Access",
                new BigDecimal("899.00"),
                "30 days full access to all quizzes and exams",
                30,
                720);

        log.info("✅ Package seeding completed - {} packages in database",
                packageRepository.count());
    }

    private void createPackageIfNotExists(PackageType type, String name, BigDecimal price,
            String description, Integer days, Integer hours) {
        if (packageRepository.findByPackageType(type).isEmpty()) {
            Package pkg = Package.builder()
                    .packageType(type)
                    .name(name)
                    .price(price)
                    .description(description)
                    .validityDays(days)
                    .validityHours(hours)
                    .active(true)
                    .build();

            packageRepository.save(pkg);
            log.info("📦 Created package: {} ({}kr, {} days)", name, price, days);
        }
    }
}
