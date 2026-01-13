package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.BusinessRuleViolationException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseAlreadyArchivedException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ReplaceWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @Inject
  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public void replace(Warehouse newWarehouse) {

    // Find current active warehouse
    Warehouse currentWarehouse = findActiveWarehouse(newWarehouse.businessUnitCode);

    // Validate new warehouse location
    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);

    // Validate capacity accommodation
    validateCapacityAccommodation(newWarehouse, currentWarehouse);

    // Validate stock matching
    validateStockMatching(newWarehouse, currentWarehouse);

    // Validate capacity and stock constraints
    validateCapacityAndStock(newWarehouse, location);

    // Archive the current warehouse
    currentWarehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(currentWarehouse);

    // Create the new warehouse with same business unit code
    newWarehouse.businessUnitCode = currentWarehouse.businessUnitCode;
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);

    LOGGER.infof("Warehouse replaced successfully: businessUnitCode=%s, oldLocation=%s, newLocation=%s",
        newWarehouse.businessUnitCode, currentWarehouse.location, newWarehouse.location);
  }

  private void validateCapacityAndStock(Warehouse warehouse, Location location) {
    // Validate capacity doesn't exceed location's maximum
    if (warehouse.capacity > location.maxCapacity) {
      throw new BusinessRuleViolationException(
          "Warehouse capacity (" + warehouse.capacity + ") exceeds location maximum capacity (" + location.maxCapacity
              + ")");
    }

    // Validate stock doesn't exceed warehouse capacity
    if (warehouse.stock > warehouse.capacity) {
      throw new BusinessRuleViolationException(
          "Warehouse stock (" + warehouse.stock + ") cannot exceed warehouse capacity (" + warehouse.capacity + ")");
    }
  }

  private void validateStockMatching(Warehouse newWarehouse, Warehouse currentWarehouse) {
    if (!newWarehouse.stock.equals(currentWarehouse.stock)) {
      throw new BusinessRuleViolationException(
          "New warehouse stock (" + newWarehouse.stock + ") must match current warehouse stock ("
              + currentWarehouse.stock + ")");
    }
  }

  private void validateCapacityAccommodation(Warehouse newWarehouse, Warehouse currentWarehouse) {
    if (newWarehouse.capacity < currentWarehouse.stock) {
      throw new BusinessRuleViolationException(
          "New warehouse capacity (" + newWarehouse.capacity + ") must be able to accommodate current warehouse stock ("
              + currentWarehouse.stock + ")");
    }
  }

  private Warehouse findActiveWarehouse(String businessUnitCode) {

    Warehouse warehouse = warehouseStore.findByBusinessUnitCode(businessUnitCode);

    if (warehouse == null) {
      throw new WarehouseNotFoundException(
          "Warehouse not found with business unit code: " + businessUnitCode);
    }

    if (warehouse.archivedAt != null) {
      throw new WarehouseAlreadyArchivedException(businessUnitCode, "replaced");
    }

    return warehouse;
  }

}
