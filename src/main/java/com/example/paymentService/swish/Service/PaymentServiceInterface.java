package com.example.paymentService.swish.Service;

import java.util.List;

import com.example.paymentService.Dto.CreateManualPaymentDTO;
import com.example.paymentService.Dto.CreatePaymentRequestDTO;
import com.example.paymentService.Dto.ManualPaymentResponseDTO;
import com.example.paymentService.Dto.PaymentResponseDTO;
import com.example.paymentService.Entity.Payment;
import com.example.paymentService.Exception.PaymentNotFoundException;
import com.example.paymentService.swish.Dto.SwishPaymentResponse;

/**
 * Service interface för betalningshantering.
 * 
 * Definierar kontrakt för alla betalningsoperationer i systemet.
 * Följer Dependency Inversion Principle - controllers och andra services
 * är beroende av detta interface, inte implementationen.
 * 
 * Ansvar:
 * - Initiera nya betalningar
 * - Hantera callbacks från betalningsleverantör (Swish)
 * - Uppdatera betalningsstatus
 * - Validera betalningar
 */
public interface PaymentServiceInterface {

    /**
     * Initierar en ny betalning via Swish.
     * 
     * Flow:
     * 1. Validera att package finns och hämta pris
     * 2. Skapa Payment entity med status CREATED
     * 3. Generera unikt instructionUUID
     * 4. Spara i databas
     * 5. Anropa Swish API för att skapa payment request
     * 6. Uppdatera status till PENDING
     * 7. Returnera PaymentResponseDTO med payment ID
     * 
     * @param request DTO med payerAlias och packageId
     * @return PaymentResponseDTO med paymentId som frontend kan använda
     * @throws IllegalArgumentException om package inte finns
     * @throws RuntimeException         om Swish-anropet misslyckas
     */
    PaymentResponseDTO initiatePayment(CreatePaymentRequestDTO request);

    /**
     * Hanterar callback från Swish när betalningsstatus ändras.
     * 
     * Flow:
     * 1. Hitta Payment via instructionUUID (paymentId)
     * 2. Validera att payment finns och inte redan är processad
     * 3. Uppdatera status baserat på Swish-response
     * 4. Om PAID: sätt paidAt timestamp, spara paymentReference
     * 5. Om ERROR: spara errorCode och errorMessage
     * 6. Spara uppdaterad Payment
     * 7. (Future) Notifiera userService, skicka kvitto
     * 
     * @param swishCallback Response från Swish med status, paymentReference, etc.
     * @throws IllegalArgumentException om payment inte hittas
     */

    void handleSwishCallback(SwishPaymentResponse swishCallback);

    /**
     * Hämtar en betalning baserat på ID.
     * 
     * @param paymentId instructionUUID för betalningen
     * @return Payment entity
     * @throws IllegalArgumentException om payment inte hittas
     */
    Payment getPaymentById(String paymentId);

    /**
     * Hämtar alla betalningar i systemet.
     * Används av admin för att övervaka alla betalningar.
     * 
     * @return Lista med alla payments
     */
    List<Payment> getAllPayments();

    /**
     * Hämtar alla betalningar för en specifik användare.
     * Används av admin eller användaren själv.
     * 
     * @param userId Användarens ID
     * @return Lista med användarens payments
     */
    List<Payment> getPaymentsByUserId(Long userId);

    /**
     * Hämtar en betalning med authorization-kontroll.
     * 
     * SÄKERHET:
     * - Admin kan se ALLA betalningar
     * - Vanlig användare kan ENDAST se EGNA betalningar
     * 
     * @param paymentId instructionUUID för betalningen
     * @param userId    Användarens ID från JWT
     * @param isAdmin   true om användaren har ROLE_ADMIN
     * @return Payment entity
     * @throws PaymentNotFoundException om payment inte finns
     * @throws ForbiddenException       om användaren saknar behörighet
     */
    Payment getPaymentByIdWithAuthorization(String paymentId, Long userId, boolean isAdmin);

    /**
     * Radera alla betalningar för en användare (Cascade Delete)
     * Används vid användarradering från UserService.
     * 
     * @param userId Användarens ID
     */
    void deletePaymentsByUserId(Long userId);

    /**
     * Skapar en manuell betalning (utan Swish integration)
     * Används när admin skapar användare + prenumeration manuellt.
     * 
     * Flow:
     * 1. Validera att user och package finns
     * 2. Hämta package pris
     * 3. Skapa Payment med status PAID och paymentMethod MANUAL
     * 4. Spara och returnera Payment
     * 
     * @param request DTO med userId och packageId
     * @return Skapad betalning med status PAID
     * @throws PackageNotFoundException om package inte finns
     */
    Payment createManualPayment(CreateManualPaymentDTO request);

    /**
     * Skapar en manuell betalning OCH returnerar DTO med package info
     * Samma som createManualPayment men returnerar ManualPaymentResponseDTO
     * 
     * @param request DTO med userId och packageId
     * @return DTO med payment + package details
     * @throws PackageNotFoundException om package inte finns
     */
    ManualPaymentResponseDTO createManualPaymentWithResponse(CreateManualPaymentDTO request);

}
