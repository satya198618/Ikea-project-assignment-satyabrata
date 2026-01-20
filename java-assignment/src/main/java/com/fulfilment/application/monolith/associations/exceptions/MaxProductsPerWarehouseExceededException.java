package com.fulfilment.application.monolith.associations.exceptions;

public class MaxProductsPerWarehouseExceededException extends AssociationDomainException {
    public MaxProductsPerWarehouseExceededException(String warehouseBusinessUnitCode) {
        super(
                String.format(
                        "Maximum number of products (5) exceeded for warehouse %s",
                        warehouseBusinessUnitCode));
    }
}
