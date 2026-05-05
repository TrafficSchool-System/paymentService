package com.example.paymentService.features.packages.service;

import com.example.paymentService.features.packages.dto.CreatePackageRequestDTO;
import com.example.paymentService.features.packages.entity.Package;
import com.example.paymentService.features.packages.entity.PackageType;
import com.example.paymentService.features.packages.repository.PackageRepository;
import com.example.paymentService.shared.exception.PackageAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * USE CASE: Create Package
 * 
 * Handles creation of new package with validation:
 * 1. Validates name is provided and not blank
 * 2. Validates price is greater than 0
 * 3. Validates validityDays is greater than 0
 * 4. Checks name uniqueness
 * 5. Creates package with active=true by default
 * 
 * USED BY:
 * - AdminPackageController: Admin creating new packages
 * 
 * This is a dedicated use-case service following vertical slice architecture.
 */
@Service
public class CreatePackageUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreatePackageUseCase.class);

    private final PackageRepository packageRepository;

    public CreatePackageUseCase(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    /**
     * Create a new package.
     * 
     * @param request Package creation details (name, price, description,
     *                validityDays)
     * @return Created package entity
     * @throws IllegalArgumentException      if validation fails
     * @throws PackageAlreadyExistsException if name already exists
     */
    @Transactional
    public Package execute(CreatePackageRequestDTO request) {
        log.info("Creating new package: {}", request.getName());

        // STEG 1: VALIDERA - Namn är obligatoriskt
        validateName(request.getName());

        // STEG 2: VALIDERA - Pris måste vara större än 0
        validatePrice(request.getPrice());

        // STEG 3: VALIDERA - ValidityDays måste vara större än 0
        validateValidityDays(request.getValidityDays());

        // STEG 4: KONTROLLERA - Namn får inte redan finnas
        checkNameUniqueness(request.getName());

        // STEG 5: SKAPA - Nytt package från request
        Package pkg = buildPackageFromRequest(request);

        // STEG 6: SPARA - Persistera till databas
        Package saved = packageRepository.save(pkg);
        log.info("✅ Package created: ID={}, Name={}, Price={} SEK",
                saved.getId(), saved.getName(), saved.getPrice());

        return saved;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            log.error("❌ Validation failed: Name is required");
            throw new IllegalArgumentException("Name is required");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("❌ Validation failed: Price must be greater than 0");
            throw new IllegalArgumentException("Price must be greater than 0");
        }
    }

    private void validateValidityDays(Integer validityDays) {
        if (validityDays == null || validityDays <= 0) {
            log.error("❌ Validation failed: ValidityDays must be greater than 0");
            throw new IllegalArgumentException("ValidityDays must be greater than 0");
        }
    }

    private void checkNameUniqueness(String name) {
        if (packageRepository.existsByName(name)) {
            log.error("❌ Package with name '{}' already exists", name);
            throw new PackageAlreadyExistsException("Package with name '" + name + "' already exists");
        }
    }

    private Package buildPackageFromRequest(CreatePackageRequestDTO request) {
        return Package.builder()
                .packageType(request.getPackageType() != null ? request.getPackageType() : PackageType.DAY)
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .validityDays(request.getValidityDays())
                .active(true) // Nya paket är aktiva som standard
                .build();
    }
}
