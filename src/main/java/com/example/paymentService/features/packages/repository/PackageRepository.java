package com.example.paymentService.features.packages.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.paymentService.features.packages.entity.Package;
import com.example.paymentService.features.packages.entity.PackageType;

import java.util.Optional;

/**
 * Repository för Package-entity.
 *
 * Ansvarar för att hantera databasanrop relaterade till paket
 * (Package) i systemet.
 *
 * Ärver från JpaRepository, vilket innebär att den erbjuder
 * standardmetoder för CRUD-operationer:
 * - save
 * - findById
 * - findAll
 * - delete
 *
 * Extra metoder definierade här:
 * - existsByName(String name) : kontrollerar om ett paket med
 * ett specifikt namn redan finns i databasen.
 */

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {

    boolean existsByName(String name);

    /**
     * Finds a package by its exact name.
     * 
     * @param name The exact package name to search for
     * @return Optional containing the package if found
     */
    Optional<Package> findByName(String name);

    /**
     * Finds a package by its type.
     * 
     * @deprecated Use findByName instead to support multiple packages per type
     * @param packageType The package type to search for
     * @return Optional containing the package if found
     */
    @Deprecated
    Optional<Package> findByPackageType(PackageType packageType);

    /**
     * Finds all packages by their type.
     * Allows multiple packages of the same type (e.g., 1 month and 3 months).
     * 
     * @param packageType The package type to search for
     * @return List of packages matching the type
     */
    java.util.List<Package> findAllByPackageType(PackageType packageType);

    /**
     * Finds all packages by active status.
     * Used to filter active packages for users.
     * 
     * @param active Active status (true = active, false = inactive)
     * @return List of packages matching the status
     */
    java.util.List<Package> findByActive(Boolean active);

}
