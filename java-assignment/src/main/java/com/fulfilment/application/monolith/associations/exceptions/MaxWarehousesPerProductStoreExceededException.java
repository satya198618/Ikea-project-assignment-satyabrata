package com.fulfilment.application.monolith.associations.exceptions;

public class MaxWarehousesPerProductStoreExceededException extends AssociationDomainException {
    public MaxWarehousesPerProductStoreExceededException(Long productId, Long storeId) {
        super(
                String.format(
                        "Maximum number of warehouses (2) exceeded for product %d at store %d",
                        productId, storeId));
    }
}
