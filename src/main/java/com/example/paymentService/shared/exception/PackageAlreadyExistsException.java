package com.example.paymentService.shared.exception;

public class PackageAlreadyExistsException extends RuntimeException {

    public PackageAlreadyExistsException(String message) {
        super(message);
    }

    public PackageAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}