package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.BusinessRuleViolationException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseAlreadyExistsException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;
  private final WarehouseRepository warehouseRepository;

  @Inject
  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver,
      WarehouseRepository warehouseRepository) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
    this.warehouseRepository = warehouseRepository;
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {

    // Business Unit Code Verification
    validateBusinessUnitCodeUniqueness(warehouse.businessUnitCode);

    // location validation
    try {
      Location location = locationResolver.resolveByIdentifier(warehouse.location);

      // warehouse creation feasibility
      validateWarehouseCreateFeasibility(location);

      // capacity and stock validation
      validateCapacityAndStock(warehouse, location);

      warehouse.createdAt = LocalDateTime.now();
      warehouse.archivedAt = null;

      warehouseStore.create(warehouse);
      LOGGER.infof("Warehouse created successfully: businessUnitCode=%s, location=%s",
          warehouse.businessUnitCode, warehouse.location);
    } catch (LocationNotFoundException | BusinessRuleViolationException e) {
      LOGGER.errorf(e, "Failed to create warehouse: businessUnitCode=%s, error=%s",
          warehouse.businessUnitCode, e.getMessage());
      throw e;
    }
  }

  private void validateCapacityAndStock(Warehouse warehouse, Location location) {

    // Validate capacity doesn't exceed location's maximum
    if (warehouse.capacity > location.maxCapacity) {
      throw new BusinessRuleViolationException("Warehouse capacity (" + warehouse.capacity
          + ") exceeds location maximum capacity (" + location.maxCapacity + ")");
    }

    // Validate stock doesn't exceed warehouse capacity
    if (warehouse.stock > warehouse.capacity) {
      throw new BusinessRuleViolationException(
          "Warehouse stock (" + warehouse.stock + ") cannot exceed warehouse capacity (" + warehouse.capacity + ")");
    }

  }

  private void validateWarehouseCreateFeasibility(Location location) {
    long currentCount = warehouseRepository.countActiveWarehousesByLocation(location.identification);

    if (currentCount >= location.maxNumberOfWarehouses) {
      throw new BusinessRuleViolationException("Maximum number of warehouses (" + location.maxNumberOfWarehouses
          + ") reached for location: " + location.identification);
    }
  }

  private void validateBusinessUnitCodeUniqueness(String businessUnitCode) {
    Warehouse existing = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
    if (existing != null && existing.archivedAt == null) {
      throw new WarehouseAlreadyExistsException(businessUnitCode);
    }
  }

}
