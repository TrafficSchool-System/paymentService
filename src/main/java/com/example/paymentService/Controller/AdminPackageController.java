package com.example.paymentService.Controller;

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

import com.example.paymentService.Dto.CreatePackageRequestDTO;
import com.example.paymentService.Dto.UpdatePackageRequestDTO;
import com.example.paymentService.Service.PackageService;
import com.example.paymentService.Entity.Package;

import jakarta.validation.Valid;

/**
 * ADMIN PACKAGE CONTROLLER
 * 
 * RESTful endpoints for admin package management operations.
 * Base path: /api/admin/packages
 * 
 * ADMIN OPERATIONS:
 * - GET /admin/packages : List ALL packages (active + inactive)
 * - POST /admin/packages : Create new package
 * - PUT /admin/packages/{id} : Update package
 * - PUT /admin/packages/{id}/status : Toggle active status
 * - DELETE /admin/packages/{id} : Delete package
 * 
 * AUTHENTICATION:
 * - All endpoints require ADMIN role
 * - Uses X-User-Role header from API Gateway
 * 
 * DESIGN PATTERN:
 * - Admin endpoints separated from user endpoints
 * - Focus on package CRUD operations
 * - Used by admin dashboard for package configuration
 */
@RestController
@RequestMapping("/api/admin/packages")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPackageController {

    private static final Logger log = LoggerFactory.getLogger(AdminPackageController.class);

    private final PackageService packageService;

    public AdminPackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    /**
     * LIST ALL PACKAGES (ADMIN)
     * GET /api/admin/packages
     * 
     * Returns ALL packages including inactive ones.
     * Used by admin panel to manage all packages.
     * 
     * @return List of all packages
     */
    @GetMapping
    public ResponseEntity<List<Package>> getAllPackages() {
        log.info("📦 GET /api/admin/packages - Admin fetching all packages");

        List<Package> packages = packageService.getAllPackages();
        log.info("✅ Found {} packages (active + inactive)", packages.size());

        return ResponseEntity.ok(packages);
    }

    /**
     * CREATE PACKAGE
     * POST /api/admin/packages
     * 
     * Creates a new payment package.
     * Used by admin to configure available payment options.
     * 
     * @param request Package details (name, price, description, validityDays)
     * @return Created package with generated ID
     */
    @PostMapping
    public ResponseEntity<Package> createPackage(@Valid @RequestBody CreatePackageRequestDTO request) {
        log.info("➕ POST /api/admin/packages - Creating new package: {}", request.getName());

        Package created = packageService.createPackage(request);
        log.info("✅ Package created with ID: {}", created.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * UPDATE PACKAGE
     * PUT /api/admin/packages/{id}
     * 
     * Updates existing package configuration.
     * Used to modify pricing, description, or validity period.
     * 
     * @param id      Package ID to update
     * @param request Updated package details
     * @return Updated package
     */
    @PutMapping("/{id}")
    public ResponseEntity<Package> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePackageRequestDTO request) {
        log.info("🔄 PUT /api/admin/packages/{} - Updating package", id);

        Package updated = packageService.updatePackage(id, request);
        log.info("✅ Package updated: {}", updated.getName());

        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE PACKAGE
     * DELETE /api/admin/packages/{id}
     * 
     * Removes a package from the system.
     * Note: This may affect existing subscriptions using this package.
     * 
     * @param id Package ID to delete
     * @return Empty response with 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        log.info("🗑️ DELETE /api/admin/packages/{} - Deleting package", id);

        packageService.deletePackage(id);
        log.info("✅ Package deleted successfully");

        return ResponseEntity.noContent().build();
    }

    /**
     * TOGGLE PACKAGE STATUS
     * PUT /api/admin/packages/{id}/status
     * 
     * Activates or deactivates a package.
     * Inactive packages are hidden from users but remain in the system.
     * 
     * @param id      Package ID
     * @param request Status request with active boolean
     * @return Updated package with new status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Package> togglePackageStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Boolean> request) {
        Boolean active = request.get("active");
        log.info("🔄 PUT /api/admin/packages/{}/status - Setting active={}", id, active);

        Package updated = packageService.togglePackageStatus(id, active);
        log.info("✅ Package status updated: {} is now {}", updated.getName(), active ? "active" : "inactive");

        return ResponseEntity.ok(updated);
    }
}
