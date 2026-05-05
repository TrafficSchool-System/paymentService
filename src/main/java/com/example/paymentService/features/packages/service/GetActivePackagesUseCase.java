package com.example.paymentService.features.packages.service;

import com.example.paymentService.features.packages.entity.Package;
import com.example.paymentService.features.packages.repository.PackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * USE CASE: Get Active Packages
 * 
 * Handles retrieval of only active packages.
 * 
 * USED BY:
 * - PackageController: Regular users should only see active packages available
 * for purchase
 * - Frontend: Displays available packages to users
 * 
 * This is a dedicated use-case service following vertical slice architecture.
 */
@Service
public class GetActivePackagesUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetActivePackagesUseCase.class);

    private final PackageRepository packageRepository;

    public GetActivePackagesUseCase(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    /**
     * Fetch only active packages from database.
     * Inactive packages are hidden from regular users.
     * 
     * @return List of active packages
     */
    public List<Package> execute() {
        log.info("Fetching active packages");
        List<Package> packages = packageRepository.findByActive(true);
        log.info("Found {} active packages", packages.size());
        return packages;
    }
}
