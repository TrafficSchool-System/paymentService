package com.example.paymentService.features.payment.service;

import com.example.paymentService.features.packages.entity.Package;
import com.example.paymentService.features.packages.service.GetPackageByIdUseCase;
import com.example.paymentService.features.payment.dto.CreateManualPaymentDTO;
import com.example.paymentService.features.payment.dto.ManualPaymentResponseDTO;
import com.example.paymentService.features.payment.entity.Payment;
import com.example.paymentService.features.payment.mapper.PaymentMapper;
import com.example.paymentService.shared.exception.PackageNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ==========================================
 * CREATE MANUAL PAYMENT WITH RESPONSE USE CASE
 * ==========================================
 * 
 * Same as CreateManualPaymentUseCase but returns enriched DTO.
 * 
 * USED BY: AdminService for complete payment + package information
 */
@Service
public class CreateManualPaymentWithResponseUseCase {

    private final CreateManualPaymentUseCase createManualPaymentUseCase;
    private final GetPackageByIdUseCase getPackageByIdUseCase;
    private final PaymentMapper paymentMapper;

    public CreateManualPaymentWithResponseUseCase(
            CreateManualPaymentUseCase createManualPaymentUseCase,
            GetPackageByIdUseCase getPackageByIdUseCase,
            PaymentMapper paymentMapper) {
        this.createManualPaymentUseCase = createManualPaymentUseCase;
        this.getPackageByIdUseCase = getPackageByIdUseCase;
        this.paymentMapper = paymentMapper;
    }

    @Transactional
    public ManualPaymentResponseDTO execute(CreateManualPaymentDTO request) {
        // Create payment (reuse existing use case)
        Payment payment = createManualPaymentUseCase.execute(request);

        // Fetch package for DTO enrichment
        Package pkg = getPackageByIdUseCase.execute(payment.getPackageId());

        // Return enriched DTO
        return paymentMapper.toManualPaymentResponse(payment, pkg);
    }
}
