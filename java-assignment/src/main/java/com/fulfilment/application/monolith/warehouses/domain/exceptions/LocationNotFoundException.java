package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/**
 * Exception thrown when a location identifier cannot be resolved.
 */
public class LocationNotFoundException extends WarehouseDomainException {

    public LocationNotFoundException(String locationIdentifier) {
        super("Location not found with identifier: " + locationIdentifier);
    }

    public LocationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
