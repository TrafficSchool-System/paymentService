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
        log.info("📦 Hämtar alla packages");
        List<Package> packages = packageRepository.findAll();
        log.info("✅ Hittade {} packages", packages.size());
        return packages;
    }

    /**
     * Hämtar endast aktiva packages.
     * Används för vanliga användare som endast ska se tillgängliga paket.
     */
    @Override
    public List<Package> getActivePackages() {
        log.info("📦 Hämtar aktiva packages");
        List<Package> packages = packageRepository.findByActive(true);
        log.info("✅ Hittade {} aktiva packages", packages.size());
        return packages;
    }

    /**
     * Hämtar ett specifikt package.
     */

    @Override
    public Package getPackageById(Long id) {
        log.info("🔍 Hämtar package med ID: {}", id);
        return packageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Package med ID {} finns inte", id);
                    return new PackageNotFoundException("Package med ID " + id + " finns inte");
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
        log.info("➕ Skapar nytt package: {}", request.getName());

        // Validering: Namn är obligatoriskt
        if (request.getName() == null || request.getName().isBlank()) {
            log.error("❌ Validation failed: Namn är obligatoriskt");
            throw new IllegalArgumentException("Namn är obligatoriskt");
        }

        // Validering: Pris är obligatoriskt
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("❌ Validation failed: Pris måste vara större än 0");
            throw new IllegalArgumentException("Pris måste vara större än 0");
        }

        // Kontrollera att namn inte redan finns
        if (packageRepository.existsByName(request.getName())) {
            log.error("❌ Package med namn '{}' finns redan", request.getName());
            throw new PackageAlreadyExistsException("Package med namn '" + request.getName() + "' finns redan");
        }

        // Validering: validityDays är obligatoriskt
        if (request.getValidityDays() == null || request.getValidityDays() <= 0) {
            log.error("❌ Validation failed: ValidityDays måste vara större än 0");
            throw new IllegalArgumentException("ValidityDays måste vara större än 0");

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
        log.info("✅ Package skapat: ID={}, Namn={}, Pris={} SEK",
                saved.getId(), saved.getName(), saved.getPrice());

        return saved;
    }

    /**
     * Uppdaterar ett befintligt package.
     */

    @Override
    @Transactional
    public Package updatePackage(Long id, UpdatePackageRequestDTO request) {
        log.info("🔄 Uppdaterar package med ID: {}", id);

        // Hämta befintligt paket
        Package existing = packageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Package med ID {} finns inte", id);
                    return new PackageNotFoundException("Package med ID " + id + " finns inte");
                });

        // Validera: Namn är obligatoriskt
        if (request.getName() == null || request.getName().isBlank()) {
            log.error("❌ Validation failed: Namn är obligatoriskt");
            throw new IllegalArgumentException("Namn är obligatoriskt");
        }

        // Validering: Pris är obligatoriskt och måste vara större än 0
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("❌ Validation failed: Pris måste vara större än 0");
            throw new IllegalArgumentException("Pris måste vara större än 0");
        }

        // Kontrollera om nytt namn koliderar med annat package
        if (!existing.getName().equals(request.getName()) &&
                packageRepository.existsByName(request.getName())) {
            log.error("❌ Package med namn '{}' finns redan", request.getName());
            throw new PackageAlreadyExistsException("Package med namn '" + request.getName() + "' finns redan");
        }

        // Uppdatera fält från DTO
        existing.setName(request.getName());
        existing.setPrice(request.getPrice());
        existing.setDescription(request.getDescription());
        existing.setValidityDays(request.getValidityDays());
        // validityHours sätts automatiskt via setter

        // Spara uppdaterat package
        Package saved = packageRepository.save(existing);
        log.info("✅ Package uppdaterat: ID={}, Namn={}, Pris={} SEK",
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
        log.info("🗑️ Tar bort package med ID: {}", id);

        // Kontrollera att package finns
        if (!packageRepository.existsById(id)) {
            log.error("❌ Package med ID {} finns inte", id);
            throw new PackageNotFoundException("Package med ID " + id + " finns inte");
        }

        // TODO: Kontrollera om package har payments innan borttagning
        // List<Payment> payments = paymentRepository.findByPackageId(id);
        // if (!payments.isEmpty()) {
        // throw new IllegalStateException("Kan inte ta bort package med aktiva
        // betalningar");
        // }

        packageRepository.deleteById(id);
        log.info("✅ Package med ID {} borttaget", id);
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
        log.info("🔄 Toggling package status: ID={}, newStatus={}", id, active);

        // Hämta befintligt paket
        Package existing = packageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Package med ID {} finns inte", id);
                    return new PackageNotFoundException("Package med ID " + id + " finns inte");
                });

        // Uppdatera active status
        existing.setActive(active);

        // Spara uppdaterat package
        Package saved = packageRepository.save(existing);
        log.info("✅ Package status updated: ID={}, Active={}", saved.getId(), saved.getActive());

        return saved;
    }
}