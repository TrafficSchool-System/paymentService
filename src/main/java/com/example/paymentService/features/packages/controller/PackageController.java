package com.example.paymentService.features.packages.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.paymentService.features.packages.service.GetActivePackagesUseCase;
import com.example.paymentService.features.packages.service.GetPackageByIdUseCase;
import com.example.paymentService.features.packages.entity.Package;

import jakarta.validation.Valid;

/**
 * PACKAGE CONTROLLER
 * 
 * RESTful endpoints for user package operations.
 * Base path: /api/packages
 * 
 * USER OPERATIONS:
 * - GET /packages : List available packages
 * - GET /packages/{id} : Get package details
 * 
 * ADMIN OPERATIONS:
 * - See AdminPackageController for admin package management
 * 
 * AUTHENTICATION:
 * - All endpoints require authentication
 */
@RestController
@RequestMapping("/api/packages")
@PreAuthorize("isAuthenticated()")
public class PackageController {

    private static final Logger log = LoggerFactory.getLogger(PackageController.class);

    private final GetActivePackagesUseCase getActivePackagesUseCase;
    private final GetPackageByIdUseCase getPackageByIdUseCase;

    /**
     * Constructor injection av use cases.
     */
    public PackageController(
            GetActivePackagesUseCase getActivePackagesUseCase,
            GetPackageByIdUseCase getPackageByIdUseCase) {
        this.getActivePackagesUseCase = getActivePackagesUseCase;
        this.getPackageByIdUseCase = getPackageByIdUseCase;
    }

    /**
     * LIST ALL PACKAGES
     * GET /api/packages
     * 
     * Returns all active packages for purchase.
     * Used by frontend to display package options to users.
     * Only shows active packages - inactive packages are hidden.
     * 
     * @return List of active packages
     */
    @GetMapping
    public ResponseEntity<List<Package>> getAllPackages() {
        log.info("📦 GET /api/packages - Fetching active packages for user");

        List<Package> packages = getActivePackagesUseCase.execute();
        log.info("✅ Found {} active packages", packages.size());

        return ResponseEntity.ok(packages);
    }

    /**
     * GET PACKAGE BY ID
     * GET /api/packages/{id}
     * 
     * Returns details for a specific package.
     * Used to display package information before purchase.
     * 
     * @param id Package ID to retrieve
     * @return Package details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Package> getPackageById(@PathVariable Long id) {
        log.info("🔍 GET /api/packages/{} - Fetching package", id);

        Package pkg = getPackageByIdUseCase.execute(id);
        log.info("✅ Package found: {}", pkg.getName());

        return ResponseEntity.ok(pkg);
    }

}
