package com.example.paymentService.features.packages.service;

import com.example.paymentService.features.packages.entity.Package;
import com.example.paymentService.features.packages.repository.PackageRepository;
import com.example.paymentService.shared.exception.PackageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * USE CASE: Toggle Package Status
 * 
 * Handles activation/deactivation of packages:
 * 1. Verifies package exists
 * 2. Updates active flag
 * 3. Saves changes
 * 
 * USED BY:
 * - AdminPackageController: Admin activating/deactivating packages
 * 
 * This allows soft deactivation instead of deletion.
 * Inactive packages are hidden from users but remain in system.
 * 
 * This is a dedicated use-case service following vertical slice architecture.
 */
@Service
public class TogglePackageStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(TogglePackageStatusUseCase.class);

    private final PackageRepository packageRepository;

    public TogglePackageStatusUseCase(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    /**
     * Toggle package active status.
     * 
     * @param id     Package ID to toggle
     * @param active New active status (true = active, false = inactive)
     * @return Updated package entity
     * @throws PackageNotFoundException if package doesn't exist
     */
    @Transactional
    public Package execute(Long id, Boolean active) {
        log.info("Toggling package status: ID={}, newStatus={}", id, active);

        // STEG 1: HÄMTA - Befintligt package
        Package existing = packageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Package with ID {} does not exist", id);
                    return new PackageNotFoundException("Package with ID " + id + " not found");
                });

        // STEG 2: UPPDATERA - Active status
        existing.setActive(active);

        // STEG 3: SPARA - Persistera ändringar
        Package saved = packageRepository.save(existing);
        log.info("✅ Package status updated: ID={}, Active={}", saved.getId(), saved.getActive());

        return saved;
    }
}
