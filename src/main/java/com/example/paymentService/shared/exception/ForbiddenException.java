package com.example.paymentService.shared.exception;

/**
 * Exception för när användare saknar behörighet till en resurs.
 * 
 * Exempel: User A försöker hämta User B:s betalning
 * 
 * GlobalExceptionHandler returnerar 403 Forbidden.
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
}