package com.fulfilment.application.monolith.associations.exceptions;

public class MaxWarehousesPerStoreExceededException extends AssociationDomainException {
    public MaxWarehousesPerStoreExceededException(Long storeId) {
        super(String.format("Maximum number of warehouses (3) exceeded for store %d", storeId));
    }
}
