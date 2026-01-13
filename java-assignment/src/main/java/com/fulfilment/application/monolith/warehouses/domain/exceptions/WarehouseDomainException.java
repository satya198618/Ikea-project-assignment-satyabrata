package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/**
 * Base exception for all warehouse domain exceptions.
 * Extends RuntimeException to avoid forcing try-catch everywhere.
 */
public class WarehouseDomainException extends RuntimeException {

    public WarehouseDomainException(String message) {
        super(message);
    }

    public WarehouseDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
