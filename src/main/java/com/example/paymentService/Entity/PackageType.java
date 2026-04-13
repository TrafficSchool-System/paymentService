package com.example.paymentService.Entity;

/**
 * Enum for package types/categories.
 * 
 * Helps categorize packages based on their duration type.
 * Multiple packages can have the same type (e.g., "1 Month" and "3 Months" both
 * have type MONTH).
 * Use package name as unique identifier instead of packageType.
 */
public enum PackageType {
    DAY, // Day packages (1-6 days) - e.g., "1 Day Access", "3 Days Access"
    WEEK, // Week packages (7-29 days) - e.g., "1 Week Access", "2 Weeks Access"
    MONTH // Month packages (30+ days) - e.g., "1 Month Access", "3 Months Access", "6
          // Months Access"
}
