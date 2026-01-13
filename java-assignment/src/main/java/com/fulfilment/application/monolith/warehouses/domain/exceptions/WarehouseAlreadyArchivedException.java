package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/**
 * Exception thrown when attempting operations on an already archived warehouse.
 */
public class WarehouseAlreadyArchivedException extends WarehouseDomainException {

    public WarehouseAlreadyArchivedException(String businessUnitCode) {
        super("Warehouse with business unit code '" + businessUnitCode + "' is already archived");
    }

    public WarehouseAlreadyArchivedException(String businessUnitCode, String operation) {
        super("Warehouse with business unit code '" + businessUnitCode + "' is already archived and cannot be "
                + operation);
    }
}
