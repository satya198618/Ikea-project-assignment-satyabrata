package com.fulfilment.application.monolith.associations.usecases;

import com.fulfilment.application.monolith.associations.AssociationRepository;
import com.fulfilment.application.monolith.associations.WarehouseProductStoreAssociation;
import com.fulfilment.application.monolith.associations.exceptions.AssociationAlreadyExistsException;
import com.fulfilment.application.monolith.associations.exceptions.MaxProductsPerWarehouseExceededException;
import com.fulfilment.application.monolith.associations.exceptions.MaxWarehousesPerProductStoreExceededException;
import com.fulfilment.application.monolith.associations.exceptions.MaxWarehousesPerStoreExceededException;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreateAssociationUseCase {

    private static final Logger LOGGER = Logger.getLogger(CreateAssociationUseCase.class);

    private static final int MAX_WAREHOUSES_PER_PRODUCT_STORE = 2;
    private static final int MAX_WAREHOUSES_PER_STORE = 3;
    private static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

    private final AssociationRepository associationRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    @Inject
    public CreateAssociationUseCase(
            AssociationRepository associationRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository) {
        this.associationRepository = associationRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public WarehouseProductStoreAssociation create(
            String warehouseBusinessUnitCode, Long productId, Long storeId) {

        // Validate entities exist
        validateWarehouseExists(warehouseBusinessUnitCode);
        validateProductExists(productId);
        validateStoreExists(storeId);

        // Check if association already exists
        validateAssociationDoesNotExist(warehouseBusinessUnitCode, productId, storeId);

        // Constraint 1: Max 2 warehouses per product per store
        validateMaxWarehousesPerProductStore(productId, storeId);

        // Constraint 2: Max 3 warehouses per store
        validateMaxWarehousesPerStore(storeId, warehouseBusinessUnitCode);

        // Constraint 3: Max 5 products per warehouse
        validateMaxProductsPerWarehouse(warehouseBusinessUnitCode, productId);

        // Create the association
        WarehouseProductStoreAssociation association = associationRepository
                .createAssociation(warehouseBusinessUnitCode, productId, storeId);

        LOGGER.infof(
                "Association created: warehouse=%s, product=%d, store=%d",
                warehouseBusinessUnitCode, productId, storeId);

        return association;
    }

    private void validateWarehouseExists(String warehouseBusinessUnitCode) {
        if (warehouseRepository.findByBusinessUnitCode(warehouseBusinessUnitCode) == null) {
            throw new IllegalArgumentException(
                    "Warehouse not found with business unit code: " + warehouseBusinessUnitCode);
        }
    }

    private void validateProductExists(Long productId) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found with id: " + productId);
        }
    }

    private void validateStoreExists(Long storeId) {
        Store store = Store.findById(storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store not found with id: " + storeId);
        }
    }

    private void validateAssociationDoesNotExist(
            String warehouseBusinessUnitCode, Long productId, Long storeId) {
        if (associationRepository
                .findByCompositeKey(warehouseBusinessUnitCode, productId, storeId)
                .isPresent()) {
            throw new AssociationAlreadyExistsException(warehouseBusinessUnitCode, productId, storeId);
        }
    }

    private void validateMaxWarehousesPerProductStore(Long productId, Long storeId) {
        long count = associationRepository.countWarehousesByProductAndStore(productId, storeId);
        if (count >= MAX_WAREHOUSES_PER_PRODUCT_STORE) {
            throw new MaxWarehousesPerProductStoreExceededException(productId, storeId);
        }
    }

    private void validateMaxWarehousesPerStore(Long storeId, String newWarehouseCode) {
        // Count distinct warehouses associated with this store
        long distinctWarehouseCount = associationRepository.countDistinctWarehousesByStore(storeId);

        // Check if the new warehouse is already associated with this store
        boolean warehouseAlreadyAssociated = associationRepository
                .findAllAssociations()
                .stream()
                .anyMatch(
                        a -> a.storeId.equals(storeId)
                                && a.warehouseBusinessUnitCode.equals(newWarehouseCode));

        // Only count as a new warehouse if it's not already associated
        if (!warehouseAlreadyAssociated && distinctWarehouseCount >= MAX_WAREHOUSES_PER_STORE) {
            throw new MaxWarehousesPerStoreExceededException(storeId);
        }
    }

    private void validateMaxProductsPerWarehouse(String warehouseBusinessUnitCode, Long newProductId) {
        // Count distinct products in this warehouse
        long distinctProductCount = associationRepository.countDistinctProductsByWarehouse(warehouseBusinessUnitCode);

        // Check if the product is already in this warehouse
        boolean productAlreadyInWarehouse = associationRepository
                .findAllAssociations()
                .stream()
                .anyMatch(
                        a -> a.warehouseBusinessUnitCode.equals(warehouseBusinessUnitCode)
                                && a.productId.equals(newProductId));

        // Only count as a new product if it's not already in the warehouse
        if (!productAlreadyInWarehouse && distinctProductCount >= MAX_PRODUCTS_PER_WAREHOUSE) {
            throw new MaxProductsPerWarehouseExceededException(warehouseBusinessUnitCode);
        }
    }
}
