package com.example.paymentService.Entity;

/** Denna klass behövs för att:
 * Swish kan retunera
 * - PAID
 * - DECLINED 
 * - ERROR
 * - CANCELLED
 * Men vi behöver även interna states: 
 * - CREATED -> Innan vi skickar till Swish.
 * - PENDING -> När vi väntar på callback.
 */

public enum PaymentStatus {
    CREATED,         // När vi precis skapat en betalning lokalt. 
    PENDING,        // Skickat till Swish, väntar på svar.
    PAID,          // Betalning genomförd. 
    DECLINED,     // Användaren nekade. 
    ERROR,       // Något gick fel   
    CANCELLED,  // Avbruten
}

