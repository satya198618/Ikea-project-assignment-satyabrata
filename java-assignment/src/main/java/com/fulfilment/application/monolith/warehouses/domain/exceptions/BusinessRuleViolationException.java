package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/**
 * Exception thrown when business rules are violated (capacity, stock, max
 * warehouses, etc.).
 */
public class BusinessRuleViolationException extends WarehouseDomainException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
