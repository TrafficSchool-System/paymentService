package com.example.paymentService.features.packages.service;

import com.example.paymentService.features.packages.dto.UpdatePackageRequestDTO;
import com.example.paymentService.features.packages.entity.Package;
import com.example.paymentService.features.packages.repository.PackageRepository;
import com.example.paymentService.shared.exception.PackageAlreadyExistsException;
import com.example.paymentService.shared.exception.PackageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * USE CASE: Update Package
 * 
 * Handles updating an existing package with validation:
 * 1. Verifies package exists
 * 2. Validates name is provided and not blank
 * 3. Validates price is greater than 0
 * 4. Checks new name doesn't collide with another package
 * 5. Updates package fields
 * 
 * USED BY:
 * - AdminPackageController: Admin updating package details
 * 
 * This is a dedicated use-case service following vertical slice architecture.
 */
@Service
public class UpdatePackageUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdatePackageUseCase.class);

    private final PackageRepository packageRepository;

    public UpdatePackageUseCase(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    /**
     * Update an existing package.
     * 
     * @param id      Package ID to update
     * @param request Updated package details (name, price, description,
     *                validityDays)
     * @return Updated package entity
     * @throws PackageNotFoundException      if package doesn't exist
     * @throws IllegalArgumentException      if validation fails
     * @throws PackageAlreadyExistsException if new name collides with another
     *                                       package
     */
    @Transactional
    public Package execute(Long id, UpdatePackageRequestDTO request) {
        log.info("Updating package with ID: {}", id);

        // STEG 1: HÄMTA - Befintligt package
        Package existing = fetchExistingPackage(id);

        // STEG 2: VALIDERA - Namn är obligatoriskt
        validateName(request.getName());

        // STEG 3: VALIDERA - Pris måste vara större än 0
        validatePrice(request.getPrice());

        // STEG 4: KONTROLLERA - Nytt namn får inte kollidera med annat package
        checkNameCollision(existing, request.getName());

        // STEG 5: UPPDATERA - Fält från request
        updatePackageFields(existing, request);

        // STEG 6: SPARA - Persistera ändringar
        Package saved = packageRepository.save(existing);
        log.info("✅ Package updated: ID={}, Name={}, Price={} SEK",
                saved.getId(), saved.getName(), saved.getPrice());

        return saved;
    }

    private Package fetchExistingPackage(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Package with ID {} does not exist", id);
                    return new PackageNotFoundException("Package with ID " + id + " not found");
                });
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

    private void checkNameCollision(Package existing, String newName) {
        if (!existing.getName().equals(newName) && packageRepository.existsByName(newName)) {
            log.error("❌ Package with name '{}' already exists", newName);
            throw new PackageAlreadyExistsException("Package with name '" + newName + "' already exists");
        }
    }

    private void updatePackageFields(Package existing, UpdatePackageRequestDTO request) {
        existing.setName(request.getName());
        existing.setPrice(request.getPrice());
        existing.setDescription(request.getDescription());
        existing.setValidityDays(request.getValidityDays());
        // validityHours sätts automatiskt via setter
    }
}
