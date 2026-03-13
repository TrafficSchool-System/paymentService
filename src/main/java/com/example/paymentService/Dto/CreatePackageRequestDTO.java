package com.example.paymentService.Dto;

import java.math.BigDecimal;
import com.example.paymentService.Entity.PackageType;

public class CreatePackageRequestDTO {
    private PackageType packageType;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer validityDays;

    // Getters & Setters
    public PackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(PackageType packageType) {
        this.packageType = packageType;
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
}