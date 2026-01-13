package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/**
 * Exception thrown when attempting to create a warehouse with a business unit
 * code that already exists.
 */
public class WarehouseAlreadyExistsException extends WarehouseDomainException {

    public WarehouseAlreadyExistsException(String businessUnitCode) {
        super("Warehouse with business unit code '" + businessUnitCode + "' already exists");
    }
}
