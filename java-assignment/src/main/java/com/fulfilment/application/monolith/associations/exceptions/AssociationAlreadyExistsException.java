package com.fulfilment.application.monolith.associations.exceptions;

public class AssociationAlreadyExistsException extends AssociationDomainException {
    public AssociationAlreadyExistsException(
            String warehouseBusinessUnitCode, Long productId, Long storeId) {
        super(
                String.format(
                        "Association already exists for warehouse %s, product %d, and store %d",
                        warehouseBusinessUnitCode, productId, storeId));
    }
}
