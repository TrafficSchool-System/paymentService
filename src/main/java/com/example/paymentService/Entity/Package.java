package com.example.paymentService.Entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity som representerar ett betalpaket i systemet.
 *
 * Ett Package innehåller information om:
 * - Namn på paketet
 * - Pris
 * - Beskrivning
 *
 * Klassen är kopplad till databastabellen "packages".
 * Används för att lagra och hantera olika typer av tjänstepaket
 * som kan köpas i systemet.
 */

import java.math.BigDecimal;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PackageType packageType = PackageType.DAY;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer validityDays; // 1, 7, 30 dagar osv

    @Column(nullable = false)
    private Integer validityHours;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true; // Package är aktivt som standard

    // Automatisk beräkning av timmar från dagar
    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
        this.validityHours = validityDays * 24; // Auto beräkna timmar
    }

    // Beräknar automatiskt innan save
    @PrePersist
    @PreUpdate
    protected void calculateValidityHours() {
        if (validityDays != null && validityHours == null) {
            this.validityHours = validityDays * 24;
        }
    }
}
