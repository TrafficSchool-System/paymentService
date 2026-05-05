package com.example.paymentService.features.payment.service;

import com.example.paymentService.features.packages.entity.Package;
import com.example.paymentService.features.packages.service.GetPackageByIdUseCase;
import com.example.paymentService.features.payment.dto.CreatePaymentRequestDTO;
import com.example.paymentService.features.payment.dto.PaymentResponseDTO;
import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.features.payment.entity.PaymentStatus;
import com.example.paymentService.features.payment.mapper.PaymentMapper;
import com.example.paymentService.features.payment.repository.PaymentRepository;
import com.example.paymentService.features.payment.integration.SwishPaymentGateway;
import com.example.paymentService.shared.exception.PackageNotFoundException;
import com.example.paymentService.shared.exception.SwishPaymentException;
import com.example.paymentService.features.payment.client.swish.SwishProperties;
import com.example.paymentService.features.payment.client.swish.dto.SwishPaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Initiates a new Swish payment transaction.
 * 
 * Orchestrates the complete payment flow: validates package, creates payment
 * entity,
 * calls Swish API via gateway, and returns payment response with deep link.
 */
@Service
public class InitiatePaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(InitiatePaymentUseCase.class);

    private static final String CURRENCY_CODE = "SEK";
    private static final String PAYMENT_REFERENCE_PREFIX = "PKG-";
    private static final String PAYMENT_MESSAGE_PREFIX = "Purchase of ";
    private static final int PAYMENT_EXPIRY_MINUTES = 5;

    private final PaymentRepository paymentRepository;
    private final GetPackageByIdUseCase getPackageByIdUseCase;
    private final SwishPaymentGateway swishGateway;
    private final SwishProperties swishProperties;
    private final PaymentMapper paymentMapper;

    public InitiatePaymentUseCase(
            PaymentRepository paymentRepository,
            GetPackageByIdUseCase getPackageByIdUseCase,
            SwishPaymentGateway swishGateway,
            SwishProperties swishProperties,
            PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.getPackageByIdUseCase = getPackageByIdUseCase;
        this.swishGateway = swishGateway;
        this.swishProperties = swishProperties;
        this.paymentMapper = paymentMapper;
    }

    @Transactional
    public PaymentResponseDTO execute(CreatePaymentRequestDTO request, Long userId) {
        log.info("Initiating payment for userId={} packageId={}", userId, request.getPackageId());

        if (userId == null) {
            log.error("❌ UserId is NULL - Authentication failed!");
            throw new IllegalArgumentException("User ID cannot be null");
        }

        Package pkg = fetchPackage(request.getPackageId());
        Payment payment = createInitialPayment(request, userId, pkg);

        try {
            processSwishPayment(payment, request, pkg);
            return paymentMapper.toPaymentResponse(payment);

        } catch (Exception e) {
            handlePaymentFailure(payment, e);
            throw new SwishPaymentException(
                    "Could not create payment with Swish: " + e.getMessage(), e);
        }
    }

    private Package fetchPackage(Long packageId) {
        Package pkg = getPackageByIdUseCase.execute(packageId);
        log.info("Package found: {} - Price: {} SEK", pkg.getName(), pkg.getPrice());
        return pkg;
    }

    private Payment createInitialPayment(CreatePaymentRequestDTO request, Long userId, Package pkg) {
        String paymentId = generatePaymentId();
        String callbackIdentifier = generateCallbackIdentifier();
        LocalDateTime expiryTime = calculateExpiryTime();

        Payment payment = buildPaymentEntity(request, userId, pkg, paymentId, callbackIdentifier, expiryTime);
        paymentRepository.save(payment);

        log.info("Payment created with status CREATED, ID: {}", paymentId);
        return payment;
    }

    private void processSwishPayment(Payment payment, CreatePaymentRequestDTO request, Package pkg) {
        SwishPaymentRequest swishRequest = buildSwishRequest(request, pkg, payment.getCallbackIdentifier());

        ResponseEntity<String> swishResponse = swishGateway.createPayment(payment.getId(), swishRequest);
        log.info("Swish API responded: Status {}, Location: {}",
                swishResponse.getStatusCode(), swishResponse.getHeaders().getLocation());

        updatePaymentStatus(payment, PaymentStatus.PENDING);
    }

    private void handlePaymentFailure(Payment payment, Exception error) {
        log.error("Payment failed for ID {}: {}", payment.getId(), error.getMessage(), error);

        payment.setStatus(PaymentStatus.ERROR);
        payment.setErrorMessage(error.getMessage());
        paymentRepository.save(payment);
    }

    private void updatePaymentStatus(Payment payment, PaymentStatus status) {
        payment.setStatus(status);
        paymentRepository.save(payment);
        log.info("Payment {} updated to status {}", payment.getId(), status);
    }

    private String generatePaymentId() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .toUpperCase();
    }

    private String generateCallbackIdentifier() {
        return UUID.randomUUID().toString();
    }

    private LocalDateTime calculateExpiryTime() {
        return LocalDateTime.now().plusMinutes(PAYMENT_EXPIRY_MINUTES);
    }

    private Payment buildPaymentEntity(
            CreatePaymentRequestDTO request,
            Long userId,
            Package pkg,
            String paymentId,
            String callbackIdentifier,
            LocalDateTime expiryTime) {

        return Payment.builder()
                .id(paymentId)
                .userId(userId)
                .packageId(pkg.getId())
                .payerAlias(request.getPayerAlias())
                .amount(pkg.getPrice())
                .status(PaymentStatus.CREATED)
                .callbackIdentifier(callbackIdentifier)
                .expiresAt(expiryTime)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private SwishPaymentRequest buildSwishRequest(
            CreatePaymentRequestDTO request,
            Package pkg,
            String callbackIdentifier) {

        SwishPaymentRequest swishRequest = new SwishPaymentRequest();
        swishRequest.setPayeeAlias(swishProperties.getMerchantNumber());
        swishRequest.setPayerAlias(request.getPayerAlias());
        swishRequest.setAmount(pkg.getPrice());
        swishRequest.setCurrency(CURRENCY_CODE);
        swishRequest.setCallbackUrl(swishProperties.getCallbackUrl());
        swishRequest.setPayeePaymentReference(buildPaymentReference(pkg.getId()));
        swishRequest.setMessage(buildPaymentMessage(pkg.getName()));
        swishRequest.setCallbackIdentifier(callbackIdentifier);

        return swishRequest;
    }

    private String buildPaymentReference(Long packageId) {
        return PAYMENT_REFERENCE_PREFIX + packageId + "-" + System.currentTimeMillis();
    }

    private String buildPaymentMessage(String packageName) {
        return PAYMENT_MESSAGE_PREFIX + packageName;
    }
}
