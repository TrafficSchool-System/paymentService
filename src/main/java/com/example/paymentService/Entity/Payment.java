package com.example.paymentService.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity som representerar en betalning i systemet.
 *
 * Klassen lagrar all information kopplad till en betaltransaktion,
 * exempelvis via Swish.
 *
 * Innehåller:
 * - Unikt betalnings-ID (instructionUUID)
 * - Koppling till köpt paket
 * - Betalarens nummer (payerAlias)
 * - Betalningsbelopp
 * - Status för betalningen (INITIATED, PAID, FAILED etc.)
 * - Referens från betalningsleverantör
 * - Tidsstämplar för skapad och genomförd betalning
 * - Eventuell felinformation vid misslyckad betalning
 *
 * Mappas mot databastabellen "payments".
 */

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @Column(length = 32)
    private String id;
    // instructionUUID (32 tecken, uppercase, utan bindestreck)

    @Column(nullable = false)
    private Long userId; // Koppla betalning till användare

    @Column(nullable = false)
    private Long packageId;

    @Column(nullable = false, length = 15)
    private String payerAlias;
    // Format: 46712345678

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(length = 50)
    private String paymentReference;
    // från Swish när PAID

    @Column(nullable = false, length = 36)
    private String callbackIdentifier;

    private LocalDateTime paidAt;

    @Column(length = 50)
    private String errorCode;

    @Column(length = 255)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    // När betalningen går ut (5 minuter från skapande)

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
