package com.example.paymentService.Entity;

/**
 * Enum for package types/categories.
 * 
 * Helps categorize packages based on their duration type.
 * Once set, packageType should not be changed to maintain data integrity.
 */
public enum PackageType {
    DAY, // Dagspaket (1-6 dagar)
    WEEK, // Veckopaket (7-29 dagar)
    MONTH // Månadspaket (30+ dagar)
}
