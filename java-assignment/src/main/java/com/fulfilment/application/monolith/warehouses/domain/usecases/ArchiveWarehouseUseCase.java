package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.InvalidWarehouseStateException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseAlreadyArchivedException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ArchiveWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  @Transactional
  public void archive(Warehouse warehouse) {

    if (warehouse == null) {
      throw new InvalidWarehouseStateException("Warehouse cannot be null");
    }

    // Verify warehouse exists
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing == null) {
      throw new WarehouseNotFoundException(warehouse.businessUnitCode);
    }

    // Check if already archived
    if (existing.archivedAt != null) {
      throw new WarehouseAlreadyArchivedException(warehouse.businessUnitCode);
    }

    // Set archive timestamp
    warehouse.archivedAt = LocalDateTime.now();

    // Delete the warehouse or update
    warehouseStore.remove(warehouse);
    LOGGER.infof("Warehouse archived successfully: businessUnitCode=%s", warehouse.businessUnitCode);
  }
}
