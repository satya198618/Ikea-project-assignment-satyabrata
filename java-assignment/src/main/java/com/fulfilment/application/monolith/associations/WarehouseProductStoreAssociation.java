package com.fulfilment.application.monolith.associations;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Cacheable
@Table(name = "warehouse_product_store_association", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "warehouse_business_unit_code", "product_id", "store_id" })
})
public class WarehouseProductStoreAssociation extends PanacheEntity {

    @Column(name = "warehouse_business_unit_code", nullable = false, length = 40)
    public String warehouseBusinessUnitCode;

    @Column(name = "product_id", nullable = false)
    public Long productId;

    @Column(name = "store_id", nullable = false)
    public Long storeId;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    public WarehouseProductStoreAssociation() {
        this.createdAt = LocalDateTime.now();
    }

    public WarehouseProductStoreAssociation(
            String warehouseBusinessUnitCode, Long productId, Long storeId) {
        this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
        this.productId = productId;
        this.storeId = storeId;
        this.createdAt = LocalDateTime.now();
    }
}
