package com.example.paymentService.Service;

import com.example.paymentService.Dto.CreatePackageRequestDTO;
import com.example.paymentService.Dto.UpdatePackageRequestDTO;
import com.example.paymentService.Entity.Package;
import com.example.paymentService.Exception.PackageAlreadyExistsException;
import com.example.paymentService.Exception.PackageNotFoundException;
import com.example.paymentService.Repository.PackageRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementering av PackageService interface.
 * 
 * Innehåller all business logic för package-hantering.
 * 
 * Dependencies:
 * - PackageRepository: För databas-operationer
 */
@Service
public class PackageService implements PackageServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(PackageService.class);

    private final PackageRepository packageRepository;

    /**
     * Constructor injection av dependencies.
     */
    public PackageService(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    /**
     * Hämtar alla packages.
     */

    @Override
    public List<Package> getAllPackages() {
        log.info("Fetching all packages");
        List<Package> packages = packageRepository.findAll();
        log.info("Found {} packages", packages.size());
        return packages;
    }

    /**
     * Hämtar endast aktiva packages.
     * Används för vanliga användare som endast ska se tillgängliga paket.
     */
    @Override
    public List<Package> getActivePackages() {
        log.info("Fetching active packages");
        List<Package> packages = packageRepository.findByActive(true);
        log.info("Found {} active packages", packages.size());
        return packages;
    }

    /**
     * Hämtar ett specifikt package.
     */

    @Override
    public Package getPackageById(Long id) {
        log.info("Fetching package with ID: {}", id);
        return packageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Package with ID {} does not exist", id);
                    return new PackageNotFoundException("Package with ID " + id + " not found");
                });
    }

    /**
     * Skapar ett nytt package.
     * 
     * @Transactional säkerställer att operationen är atomär.
     */

    @Override
    @Transactional
    public Package createPackage(CreatePackageRequestDTO request) {
        log.info("Creating new package: {}", request.getName());

        // Validering: Namn är obligatoriskt
        if (request.getName() == null || request.getName().isBlank()) {
            log.error("Validation failed: Name is required");
            throw new IllegalArgumentException("Name is required");
        }

        // Validering: Pris är obligatoriskt
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Validation failed: Price must be greater than 0");
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        // Kontrollera att namn inte redan finns
        if (packageRepository.existsByName(request.getName())) {
            log.error("Package with name '{}' already exists", request.getName());
            throw new PackageAlreadyExistsException("Package with name '" + request.getName() + "' already exists");
        }

        // Validering: validityDays är obligatoriskt
        if (request.getValidityDays() == null || request.getValidityDays() <= 0) {
            log.error("Validation failed: ValidityDays must be greater than 0");
            throw new IllegalArgumentException("ValidityDays must be greater than 0");

        }

        // Mappa DTO → Entity
        Package pkg = Package.builder()
                .packageType(request.getPackageType() != null ? request.getPackageType()
                        : com.example.paymentService.Entity.PackageType.DAY)
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .validityDays(request.getValidityDays())
                .active(true) // Nya paket är aktiva som standard
                .build();

        // Spara package
        Package saved = packageRepository.save(pkg);
        log.info("Package created: ID={}, Name={}, Price={} SEK",
                saved.getId(), saved.getName(), saved.getPrice());

        return saved;
    }

    /**
     * Uppdaterar ett befintligt package.
     */

    @Override
    @Transactional
    public Package updatePackage(Long id, UpdatePackageRequestDTO request) {
        log.info("Updating package with ID: {}", id);

        // Hämta befintligt paket
        Package existing = packageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Package with ID {} does not exist", id);
                    return new PackageNotFoundException("Package with ID " + id + " not found");
                });

        // Validera: Namn är obligatoriskt
        if (request.getName() == null || request.getName().isBlank()) {
            log.error("Validation failed: Name is required");
            throw new IllegalArgumentException("Name is required");
        }

        // Validering: Pris är obligatoriskt och måste vara större än 0
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Validation failed: Price must be greater than 0");
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        // Kontrollera om nytt namn koliderar med annat package
        if (!existing.getName().equals(request.getName()) &&
                packageRepository.existsByName(request.getName())) {
            log.error("Package with name '{}' already exists", request.getName());
            throw new PackageAlreadyExistsException("Package with name '" + request.getName() + "' already exists");
        }

        // Uppdatera fält från DTO
        existing.setName(request.getName());
        existing.setPrice(request.getPrice());
        existing.setDescription(request.getDescription());
        existing.setValidityDays(request.getValidityDays());
        // validityHours sätts automatiskt via setter

        // Spara uppdaterat package
        Package saved = packageRepository.save(existing);
        log.info("Package updated: ID={}, Name={}, Price={} SEK",
                saved.getId(), saved.getName(), saved.getPrice());
        return saved;

    }

    /**
     * Tar bort ett package.
     * 
     * OBS: I produktion bör du lägga till kontroll för:
     * - Om package har payments associerade
     * - Eventuellt soft delete istället (isActive flag)
     */

    @Override
    @Transactional
    public void deletePackage(Long id) {
        log.info("Deleting package with ID: {}", id);

        // Kontrollera att package finns
        if (!packageRepository.existsById(id)) {
            log.error("❌ Package with ID {} does not exist", id);
            throw new PackageNotFoundException("Package with ID " + id + " not found");
        }

        // TODO: Kontrollera om package har payments innan borttagning
        // List<Payment> payments = paymentRepository.findByPackageId(id);
        // if (!payments.isEmpty()) {
        // throw new IllegalStateException("Kan inte ta bort package med aktiva
        // betalningar");
        // }

        packageRepository.deleteById(id);
        log.info("✅ Package with ID {} deleted", id);
    }

    @Override
    public boolean existsByName(String name) {
        return packageRepository.existsByName(name);

    }

    /**
     * Toggles package active status.
     * 
     * @param id     Package ID
     * @param active New active status (true = active, false = inactive)
     * @return Updated package
     */
    @Override
    @Transactional
    public Package togglePackageStatus(Long id, Boolean active) {
        log.info("Toggling package status: ID={}, newStatus={}", id, active);

        // Hämta befintligt paket
        Package existing = packageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Package with ID {} does not exist", id);
                    return new PackageNotFoundException("Package with ID " + id + " not found");
                });

        // Uppdatera active status
        existing.setActive(active);

        // Spara uppdaterat package
        Package saved = packageRepository.save(existing);
        log.info("Package status updated: ID={}, Active={}", saved.getId(), saved.getActive());

        return saved;
    }
}