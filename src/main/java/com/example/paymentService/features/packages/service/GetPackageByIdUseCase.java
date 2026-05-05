package com.example.paymentService.features.packages.service;

import com.example.paymentService.features.packages.entity.Package;
import com.example.paymentService.features.packages.repository.PackageRepository;
import com.example.paymentService.shared.exception.PackageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * USE CASE: Get Package By ID
 * 
 * Handles retrieval of a specific package by its ID.
 * 
 * USED BY:
 * - PackageController: Users viewing package details
 * - AdminPackageController: Admin viewing/editing specific package
 * - InitiatePaymentUseCase: Validating package exists before payment
 * 
 * This is a dedicated use-case service following vertical slice architecture.
 */
@Service
public class GetPackageByIdUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetPackageByIdUseCase.class);

    private final PackageRepository packageRepository;

    public GetPackageByIdUseCase(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    /**
     * Find package by ID.
     * 
     * @param id Package ID to find
     * @return Package entity
     * @throws PackageNotFoundException if package doesn't exist
     */
    public Package execute(Long id) {
        log.info("Fetching package with ID: {}", id);

        return packageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Package with ID {} does not exist", id);
                    return new PackageNotFoundException("Package with ID " + id + " not found");
                });
    }
}
