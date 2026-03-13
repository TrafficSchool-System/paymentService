package com.example.paymentService.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.paymentService.Entity.Payment;
import com.example.paymentService.Entity.PaymentStatus;

/**
 * Repository för Payment-entity.
 *
 * Ansvarar för att hantera databasanrop relaterade till betalningar
 * (Payment) i systemet.
 *
 * Ärver från JpaRepository, vilket innebär att den erbjuder
 * standardmetoder för CRUD-operationer:
 * - save
 * - findById
 * - findAll
 * - delete
 *
 * Extra metoder definierade här:
 * - findByCallbackIdentifier(String callbackIdentifier) :
 * Hämtar en betalning baserat på callbackIdentifier som används
 * för att spåra betalningscallback från betalningsleverantör.
 * 
 * - findByStatus(PaymentStatus status) :
 * Hämtar alla betalningar med en specifik status (t.ex. PAID, INITIATED,
 * FAILED).
 * 
 * - findByPackageId(Long packageId) :
 * Hämtar alla betalningar kopplade till ett specifikt paket.
 */

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByCallbackIdentifier(String callbackIdentifier);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByPackageId(Long packageId);

    List<Payment> findByUserId(Long userId);
}
