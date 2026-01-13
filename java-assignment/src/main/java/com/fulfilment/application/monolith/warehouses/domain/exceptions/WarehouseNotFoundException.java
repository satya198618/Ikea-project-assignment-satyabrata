package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/**
 * Exception thrown when a warehouse is not found by ID or business unit code.
 */
public class WarehouseNotFoundException extends WarehouseDomainException {

    public WarehouseNotFoundException(String businessUnitCode) {
        super("Warehouse not found with business unit code: " + businessUnitCode);
    }
}
