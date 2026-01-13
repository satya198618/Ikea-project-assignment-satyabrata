package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/**
 * Exception thrown for invalid warehouse states (null warehouse, invalid
 * format, etc.).
 */
public class InvalidWarehouseStateException extends WarehouseDomainException {

    public InvalidWarehouseStateException(String message) {
        super(message);
    }
}
