package com.fulfilment.application.monolith.associations;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AssociationRepository implements PanacheRepository<WarehouseProductStoreAssociation> {


    public long countWarehousesByProductAndStore(Long productId, Long storeId) {
        return count(
                "productId = ?1 and storeId = ?2",
                productId, storeId);
    }


    public long countDistinctWarehousesByStore(Long storeId) {
        return find(
                "select count(distinct warehouseBusinessUnitCode) from WarehouseProductStoreAssociation where storeId = ?1",
                storeId)
                .project(Long.class)
                .firstResult();
    }


    public long countDistinctProductsByWarehouse(String warehouseBusinessUnitCode) {
        return find(
                "select count(distinct productId) from WarehouseProductStoreAssociation where warehouseBusinessUnitCode = ?1",
                warehouseBusinessUnitCode)
                .project(Long.class)
                .firstResult();
    }


    public Optional<WarehouseProductStoreAssociation> findByCompositeKey(
            String warehouseBusinessUnitCode, Long productId, Long storeId) {
        return find(
                "warehouseBusinessUnitCode = ?1 and productId = ?2 and storeId = ?3",
                warehouseBusinessUnitCode,
                productId,
                storeId)
                .firstResultOptional();
    }

    @Transactional
    public WarehouseProductStoreAssociation createAssociation(
            String warehouseBusinessUnitCode, Long productId, Long storeId) {
        WarehouseProductStoreAssociation association = new WarehouseProductStoreAssociation(warehouseBusinessUnitCode,
                productId, storeId);
        persist(association);
        return association;
    }


    @Transactional
    public void deleteAssociation(
            String warehouseBusinessUnitCode, Long productId, Long storeId) {
        delete(
                "warehouseBusinessUnitCode = ?1 and productId = ?2 and storeId = ?3",
                warehouseBusinessUnitCode,
                productId,
                storeId);
    }


    public List<WarehouseProductStoreAssociation> findAllAssociations() {
        return listAll();
    }
}
