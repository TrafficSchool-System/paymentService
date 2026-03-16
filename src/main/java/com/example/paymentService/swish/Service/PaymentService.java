package com.example.paymentService.swish.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.paymentService.Dto.CreatePaymentRequestDTO;
import com.example.paymentService.Dto.PaymentResponseDTO;
import com.example.paymentService.Dto.CreateSubscriptionRequestDTO;
import com.example.paymentService.Entity.Package;
import com.example.paymentService.Entity.Payment;
import com.example.paymentService.Entity.PaymentStatus;
import com.example.paymentService.Exception.ForbiddenException;
import com.example.paymentService.Exception.PackageNotFoundException;
import com.example.paymentService.Exception.PaymentNotFoundException;
import com.example.paymentService.Exception.SwishPaymentException;
import com.example.paymentService.Repository.PackageRepository;
import com.example.paymentService.Repository.PaymentRepository;
import com.example.paymentService.swish.Client.SwishClient;
import com.example.paymentService.swish.Config.SwishProperties;
import com.example.paymentService.swish.Dto.SwishPaymentRequest;
import com.example.paymentService.swish.Dto.SwishPaymentResponse;

import jakarta.transaction.Transactional;

/**
 * Implementering av PaymentService interface.
 * 
 * Innehåller all business logic för betalningshantering.
 * Annoterad med @Service så Spring skapar en bean av denna klass.
 * 
 * Dependencies:
 * - PaymentRepository: För databas-operationer
 * - PackageRepository: För att hämta pris på paket
 * - SwishClient: För att kommunicera med Swish API
 * - SwishProperties: För konfiguration (merchant number, callback URL)
 */

@Service
public class PaymentService implements PaymentServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PackageRepository packageRepository;
    private final SwishClient swishClient;
    private final SwishProperties swishProperties;
    private final WebClient userServiceWebClient;

    // API key för service-to-service autentisering
    @Value("${service.api.key}")
    private String serviceApiKey;

    /**
     * Constructor injection - Spring injicerar automatiskt alla dependencies.
     * Fördelaktigare än @Autowired field injection för testbarhet.
     */

    public PaymentService(
            PaymentRepository paymentRepository,
            PackageRepository packageRepository,
            SwishClient swishClient,
            SwishProperties swishProperties,
            WebClient userServiceWebClient) {
        this.paymentRepository = paymentRepository;
        this.packageRepository = packageRepository;
        this.swishClient = swishClient;
        this.swishProperties = swishProperties;
        this.userServiceWebClient = userServiceWebClient;
    }

    /**
     * Initierar en ny betalning.
     * 
     * @Transactional säkerställer att alla databas-operationer är atomära.
     *                Om något går fel rullas alla ändringar tillbaka.
     */

    @Override
    @Transactional
    public PaymentResponseDTO initiatePayment(CreatePaymentRequestDTO request) {
        log.info("Initiating payment for userId: {}, payerAlias: {}, packageId: {}",
                request.getUserId(), request.getPayerAlias(), request.getPackageId());

        // 1. Validera och hämta package
        Package pkg = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new PackageNotFoundException(
                        "Package with ID " + request.getPackageId() + " not found"));

        log.info("Package found: {} - Price: {} SEK", pkg.getName(), pkg.getPrice());

        // 2. Generera unikt payment ID (32 hex-tecken utan bindestreck, uppercase)
        String instructionUUID = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .toUpperCase();

        // 3. Generera callback identifier för säkerhet
        String callbackIdentifier = UUID.randomUUID().toString();

        // 4. Skapa payment entity med status CREATED
        Payment payment = Payment.builder()
                .id(instructionUUID)
                .userId(request.getUserId())
                .packageId(pkg.getId())
                .payerAlias(request.getPayerAlias())
                .amount(pkg.getPrice())
                .status(PaymentStatus.CREATED)
                .callbackIdentifier(callbackIdentifier)
                .createdAt(LocalDateTime.now())
                .build();

        // 5. Spara i databas (första gången - status CREATED)
        paymentRepository.save(payment);
        log.info("Payment saved in database with status CREATED, ID: {}", instructionUUID);

        // 6. Bygg Swish payment request
        SwishPaymentRequest swishRequest = new SwishPaymentRequest();
        swishRequest.setPayeeAlias(swishProperties.getMerchantNumber());
        swishRequest.setPayerAlias(request.getPayerAlias());
        swishRequest.setAmount(pkg.getPrice());
        swishRequest.setCurrency("SEK");
        swishRequest.setCallbackUrl(swishProperties.getCallbackUrl());
        swishRequest.setPayeePaymentReference("PKG-" + pkg.getId() + "-" + System.currentTimeMillis());
        swishRequest.setMessage("Purchase of " + pkg.getName());
        swishRequest.setCallbackIdentifier(callbackIdentifier);

        try {
            // 7. Skicka till Swish
            log.info("Sending payment to Swish...");
            ResponseEntity<String> swishResponse = swishClient.createPayment(
                    instructionUUID,
                    swishRequest);

            log.info("Swish response: Status {}, Location: {}",
                    swishResponse.getStatusCode(),
                    swishResponse.getHeaders().getLocation());

            // 8. Uppdatera status till PENDING (väntar på callback)
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);
            log.info("Payment updated to status PENDING");

            // 9. Bygg response till frontend
            PaymentResponseDTO response = new PaymentResponseDTO();
            response.setPaymentId(instructionUUID);

            // Swish M-Commerce: Bygg deep link och QR-kod data
            // Format: swish://paymentrequest?token=<instructionUUID>&callbackurl=<url>
            String swishDeepLink = "swish://paymentrequest?token=" + instructionUUID +
                    "&callbackurl=" + swishProperties.getCallbackUrl();
            response.setSwishDeepLink(swishDeepLink);
            response.setQrCodeData(swishDeepLink);

            // Swish betalningar går ut efter 3 minuter
            response.setExpiresAt(LocalDateTime.now().plusMinutes(5));

            return response;
        } catch (Exception e) {
            // Vid fel: uppdatera payment till ERROR status
            log.error("Error during Swish call: {}", e.getMessage(), e);
            payment.setStatus(PaymentStatus.ERROR);
            payment.setErrorMessage(e.getMessage());
            paymentRepository.save(payment);

            throw new SwishPaymentException("Could not create payment with Swish: " + e.getMessage(), e);
        }
    }

    /**
     * Hanterar callback från Swish.
     * 
     * Uppdaterar betalningsstatus baserat på Swish-response.
     * 
     * @Transactional säkerställer databas-konsistens.
     */

    @Override
    @Transactional
    public void handleSwishCallback(SwishPaymentResponse swishCallback) {
        log.info("Handling Swish callback for payment ID: {}", swishCallback.getId());

        // 1. Hitta payment i databas
        Payment payment = paymentRepository.findById(swishCallback.getId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment with ID " + swishCallback.getId() + " not found"));

        log.info("Payment found: Status before={}, Amount={}",
                payment.getStatus(), payment.getAmount());

        // 2. Kontrollera om payment redan är processad (idempotens)
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.warn("Payment is already PAID, ignoring callback");
            return;
        }

        // 3. Uppdatera baserat på Swish status
        String swishStatus = swishCallback.getStatus();

        switch (swishStatus) {
            case "PAID":
                log.info("Payment completed!");
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(LocalDateTime.now());
                payment.setPaymentReference(swishCallback.getPaymentReference());

                activateSubscriptionForUser(payment);

                // TODO: Framtida funktionalitet
                // - Anropa userService för att aktivera paket - KLART
                // - Skicka kvitto via email
                // - Logga för bokföring
                break;

            case "DECLINED":
                log.warn("Payment declined by user");
                payment.setStatus(PaymentStatus.DECLINED);
                break;

            case "ERROR":
                log.error("Error in payment: {} - {}",
                        swishCallback.getErrorCode(),
                        swishCallback.getErrorMessage());
                payment.setStatus(PaymentStatus.ERROR);
                payment.setErrorCode(swishCallback.getErrorCode());
                payment.setErrorMessage(swishCallback.getErrorMessage());
                break;

            case "CANCELLED":
                log.info("Payment cancelled");
                payment.setStatus(PaymentStatus.CANCELLED);
                break;

            default:
                log.warn("Unknown Swish status: {}", swishStatus);
                return;
        }

        // 4. Spara uppdaterad payment
        paymentRepository.save(payment);
        log.info("Payment updated to status: {}", payment.getStatus());
    }

    /**
     * Hämtar en payment via ID.
     */

    @Override
    public Payment getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment with ID " + paymentId + " not found"));
    }

    /**
     * Hämtar alla betalningar i systemet.
     * Används av admin för att övervaka alla betalningar.
     */
    @Override
    public List<Payment> getAllPayments() {
        log.info("[ADMIN] Fetching all payments");
        return paymentRepository.findAll();
    }

    /**
     * Aktiverar subscription i userService när betalning är PAID.
     * Anropar userService via WebClient.
     */
    private void activateSubscriptionForUser(Payment payment) {
        try {
            log.info("🔄 Activating subscription in userService for payment: {}", payment.getId());

            // Hämta package-info
            Package pkg = packageRepository.findById(payment.getPackageId())
                    .orElseThrow(() -> new PackageNotFoundException("Package not found: " + payment.getPackageId()));

            // Bygg request
            CreateSubscriptionRequestDTO subscriptionRequest = new CreateSubscriptionRequestDTO();
            subscriptionRequest.setUserId(payment.getUserId());
            subscriptionRequest.setPackageId(pkg.getId());
            subscriptionRequest.setPackageName(pkg.getName());
            subscriptionRequest.setPackagePrice(pkg.getPrice());
            subscriptionRequest.setValidityDays(pkg.getValidityDays());
            subscriptionRequest.setValidityHours(pkg.getValidityHours());
            subscriptionRequest.setPaymentId(payment.getId());

            // Anropa userService
            String response = userServiceWebClient.post()
                    .uri("/api/subscriptions")
                    .header("X-Internal-API-Key", serviceApiKey)
                    .bodyValue(subscriptionRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Subscription activated in userService for userId= {}, response: {}",
                    payment.getUserId(), response);

        } catch (Exception e) {
            // Logga fel men kasta INTE exception
            // Betalningen är fortfarande giltig även om subscription-skapandet misslyckas
            log.error("Error activating subscription for payment {}: {}",
                    payment.getId(), e.getMessage(), e);
        }
    }

    /**
     * Hämtar en payment med authorization-kontroll.
     * 
     * Business logic för säkerhet:
     * - Admin kan se ALLA betalningar
     * - Vanlig användare kan ENDAST se EGNA betalningar
     * 
     * Samma mönster som ExamService använder - business logic i service,
     * kastar exception som GlobalExceptionHandler fångar.
     */
    @Override
    public Payment getPaymentByIdWithAuthorization(String paymentId, Long userId, boolean isAdmin) {
        log.info("Authorization check - paymentId: {}, userId: {}, isAdmin: {}",
                paymentId, userId, isAdmin);

        // 1. Hämta payment (kastar PaymentNotFoundException om den inte finns)
        Payment payment = getPaymentById(paymentId);

        // 2. Authorization: Admin ser allt, vanlig användare endast sina egna
        if (!isAdmin && !payment.getUserId().equals(userId)) {
            log.warn("FORBIDDEN: User {} tried to access payment {} owned by user {}",
                    userId, paymentId, payment.getUserId());
            throw new ForbiddenException(
                    "You do not have permission to view this payment");
        }

        log.info("Authorization passed");
        return payment;
    }

    /**
     * Hämtar alla betalningar för en specifik användare.
     * Används av admin eller användaren själv.
     * 
     * @param userId Användarens ID
     * @return Lista med användarens payments
     */
    @Override
    public List<Payment> getPaymentsByUserId(Long userId) {
        log.info("Fetching payments for userId: {}", userId);
        List<Payment> payments = paymentRepository.findByUserId(userId);
        log.info("Found {} payments for user {}", payments.size(), userId);
        return payments;
    }

    /**
     * ==========================================
     * CASCADE DELETE - RADERA ANVÄNDARENS BETALNINGAR
     * ==========================================
     */

    /**
     * RADERA ANVÄNDARENS BETALNINGAR
     * 
     * Används vid cascade delete när en användare raderas från systemet.
     * Anropas från UserService via REST API.
     * 
     * VIKTIGT:
     * - Detta raderar betalningshistorik permanent
     * - Överväg att spara backup eller loggar om detta behövs för bokföring
     * 
     * @param userId Användarens ID
     */
    @Override
    @Transactional
    public void deletePaymentsByUserId(Long userId) {
        log.info("Cascade delete: Removing all payments for userId: {}", userId);

        List<Payment> payments = paymentRepository.findByUserId(userId);

        if (!payments.isEmpty()) {
            paymentRepository.deleteAll(payments);
            log.info("Deleted {} payments for user {}", payments.size(), userId);
        } else {
            log.info("No payments to delete for user {}", userId);
        }
    }
}
