package com.example.paymentService.features.packages.service;

import com.example.paymentService.features.packages.repository.PackageRepository;
import com.example.paymentService.shared.exception.PackageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * USE CASE: Delete Package
 * 
 * Handles deletion of a package:
 * 1. Verifies package exists
 * 2. Deletes package from database
 * 
 * USED BY:
 * - AdminPackageController: Admin deleting packages
 * 
 * NOTE: In production, you should:
 * - Check if package has associated payments before deletion
 * - Consider soft delete (active flag) instead of hard delete
 * 
 * This is a dedicated use-case service following vertical slice architecture.
 */
@Service
public class DeletePackageUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeletePackageUseCase.class);

    private final PackageRepository packageRepository;

    public DeletePackageUseCase(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    /**
     * Delete a package.
     * 
     * @param id Package ID to delete
     * @throws PackageNotFoundException if package doesn't exist
     */
    @Transactional
    public void execute(Long id) {
        log.info("Deleting package with ID: {}", id);

        // STEG 1: KONTROLLERA - Package finns
        if (!packageRepository.existsById(id)) {
            log.error("❌ Package with ID {} does not exist", id);
            throw new PackageNotFoundException("Package with ID " + id + " not found");
        }

        // TODO: VALIDERA - Kontrollera om package har payments innan borttagning
        // List<Payment> payments = paymentRepository.findByPackageId(id);
        // if (!payments.isEmpty()) {
        // throw new IllegalStateException("Cannot delete package with active
        // payments");
        // }

        // STEG 2: RADERA - Från databas
        packageRepository.deleteById(id);
        log.info("✅ Package with ID {} deleted", id);
    }
}
