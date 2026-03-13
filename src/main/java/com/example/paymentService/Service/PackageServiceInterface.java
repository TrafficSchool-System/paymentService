package com.example.paymentService.Service;

import java.util.List;

import com.example.paymentService.Dto.CreatePackageRequestDTO;
import com.example.paymentService.Dto.UpdatePackageRequestDTO;
import com.example.paymentService.Entity.Package;

/**
 * Service interface för package-hantering.
 * 
 * Definierar kontrakt för CRUD-operationer på packages.
 * Följer samma mönster som PaymentService.
 * 
 * Ansvar:
 * - Skapa nya packages
 * - Hämta packages (alla eller specifikt)
 * - Uppdatera befintliga packages
 * - Ta bort packages
 * - Validera packages
 */

public interface PackageServiceInterface {

    /**
     * Hämtar alla tillgängliga packages.
     * 
     * Används av frontend för att visa vilka paket som finns att köpa.
     * 
     * @return Lista med alla packages
     */

    List<Package> getAllPackages();

    /**
     * Hämtar alla aktiva packages.
     * 
     * Används av frontend för att visa endast aktiva paket för användare.
     * Admin kan se alla paket via getAllPackages().
     * 
     * @return Lista med aktiva packages
     */
    List<Package> getActivePackages();

    /**
     * Hämtar ett specifikt package via ID.
     * 
     * @param id Package ID
     * @return Package entity
     * @throws IllegalArgumentException om package inte hittas
     */

    Package getPackageById(Long id);

    /**
     * Skapar ett nytt package.
     * 
     * Flow:
     * 1. Validera input (namn, pris måste finnas)
     * 2. Kontrollera att namn inte redan finns
     * 3. Spara i databas
     * 
     * @param pkg Package att skapa (utan ID)
     * @return Sparat package med genererat ID
     * @throws IllegalArgumentException om validering misslyckas eller namn finns
     */

    Package createPackage(CreatePackageRequestDTO request);

    /**
     * Uppdaterar ett befintligt package.
     * 
     * Flow:
     * 1. Kontrollera att package finns
     * 2. Validera nya värden
     * 3. Kontrollera om nytt namn kolliderar med annat package
     * 4. Uppdatera och spara
     * 
     * @param id             ID för package att uppdatera
     * @param updatedPackage Package med nya värden
     * @return Uppdaterat package
     * @throws IllegalArgumentException om package inte hittas eller validering
     *                                  misslyckas
     */

    Package updatePackage(Long id, UpdatePackageRequestDTO request);

    /**
     * Tar bort ett package.
     * 
     * OBS: I produktion bör du kontrollera om package har aktiva betalningar
     * innan borttagning, eller använda soft delete (isActive flag).
     * 
     * @param id Package ID
     * @throws IllegalArgumentException om package inte hittas
     */

    void deletePackage(Long id);

    /**
     * Kontrollerar om ett package med givet namn redan finns.
     * 
     * @param name Package namn
     * @return true om namnet finns, annars false
     */
    boolean existsByName(String name);

    /**
     * Toggles the active status of a package.
     * 
     * Used by admin to activate/deactivate packages without deleting them.
     * Inactive packages are hidden from users but remain in the system.
     * 
     * @param id     Package ID
     * @param active New active status (true = active, false = inactive)
     * @return Updated package
     * @throws PackageNotFoundException if package doesn't exist
     */
    Package togglePackageStatus(Long id, Boolean active);
}
