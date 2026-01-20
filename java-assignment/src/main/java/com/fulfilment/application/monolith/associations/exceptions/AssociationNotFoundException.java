package com.fulfilment.application.monolith.associations.exceptions;

public class AssociationNotFoundException extends AssociationDomainException {
    public AssociationNotFoundException(
            String warehouseBusinessUnitCode, Long productId, Long storeId) {
        super(
                String.format(
                        "Association not found for warehouse %s, product %d, and store %d",
                        warehouseBusinessUnitCode, productId, storeId));
    }
}
