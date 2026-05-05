package com.example.paymentService.features.packages.service;

import com.example.paymentService.features.packages.entity.Package;
import com.example.paymentService.features.packages.repository.PackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * USE CASE: Get All Packages
 * 
 * Handles retrieval of all packages (active + inactive).
 * 
 * USED BY:
 * - AdminPackageController: Admin needs to see all packages including inactive
 * ones
 * 
 * This is a dedicated use-case service following vertical slice architecture.
 */
@Service
public class GetAllPackagesUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetAllPackagesUseCase.class);

    private final PackageRepository packageRepository;

    public GetAllPackagesUseCase(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    /**
     * Fetch all packages from database.
     * Returns both active and inactive packages.
     * 
     * @return List of all packages
     */
    public List<Package> execute() {
        log.info("Fetching all packages");
        List<Package> packages = packageRepository.findAll();
        log.info("Found {} packages", packages.size());
        return packages;
    }
}
