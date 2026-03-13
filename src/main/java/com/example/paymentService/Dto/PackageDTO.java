package com.example.paymentService.Dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) för Package-entity.
 *
 * Används för att överföra paketdata mellan olika lager i systemet,
 * t.ex. mellan service- och controller-lager eller vid API-respons.
 *
 * Innehåller endast de fält som behövs för presentation eller överföring:
 * - id: Paketets unika identifierare
 * - name: Paketets namn
 * - description: Kort beskrivning av paketet
 * - price: Pris för paketet
 *
 * Notera att detta objekt inte innehåller någon affärslogik eller koppling
 * direkt till databasen.
 */

public class PackageDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer validityDays;
    private Integer validityHours;

    // Standard getters & setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }

    public Integer getValidityHours() {
        return validityHours;
    }

    public void setValidityHours(Integer validityHours) {
        this.validityHours = validityHours;
    }

}
