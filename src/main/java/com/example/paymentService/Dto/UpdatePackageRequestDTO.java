package com.example.paymentService.Dto;

import java.math.BigDecimal;

public class UpdatePackageRequestDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer validityDays;
    
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