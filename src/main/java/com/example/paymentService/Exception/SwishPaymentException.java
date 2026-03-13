package com.example.paymentService.Exception;

public class SwishPaymentException extends RuntimeException {

    public SwishPaymentException(String message) {
        super(message);
    }

    public SwishPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}