package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.InvalidWarehouseStateException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class);

  @Inject
  private WarehouseRepository warehouseRepository;
  @Inject
  private CreateWarehouseOperation createWarehouseOperation;
  @Inject
  private ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject
  private ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse = toDomainWarehouse(data);
    createWarehouseOperation.create(domainWarehouse);
    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    try {
      Long warehouseId = Long.parseLong(id);
      DbWarehouse dbWarehouse = warehouseRepository.findByWarehouseId(warehouseId);

      if (dbWarehouse == null) {
        throw new NotFoundException("Warehouse not found with id: " + id);
      }

      return toWarehouseResponse(dbWarehouse.toWarehouse());
    } catch (NumberFormatException e) {
      throw new InvalidWarehouseStateException("Invalid warehouse ID format: " + id);
    }
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    try {
      Long warehouseId = Long.parseLong(id);
      DbWarehouse dbWarehouse = warehouseRepository.findByWarehouseId(warehouseId);

      if (dbWarehouse == null) {
        throw new NotFoundException("Warehouse not found with id: " + id);
      }

      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse = dbWarehouse
          .toWarehouse();
      archiveWarehouseOperation.archive(domainWarehouse);

    } catch (NumberFormatException e) {
      throw new InvalidWarehouseStateException("Invalid warehouse ID format: " + id);
    }
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, @NotNull Warehouse data) {

    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse newWarehouse = toDomainWarehouse(data);
    newWarehouse.businessUnitCode = businessUnitCode;
    replaceWarehouseOperation.replace(newWarehouse);
    return toWarehouseResponse(newWarehouse);
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainWarehouse(
      Warehouse apiWarehouse) {
    var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domain.businessUnitCode = apiWarehouse.getBusinessUnitCode();
    domain.location = apiWarehouse.getLocation();
    domain.capacity = apiWarehouse.getCapacity();
    domain.stock = apiWarehouse.getStock();
    return domain;
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);

      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", exception.getClass().getName());
      exceptionJson.put("code", code);

      if (exception.getMessage() != null) {
        exceptionJson.put("error", exception.getMessage());
      }

      return Response.status(code).entity(exceptionJson).build();
    }
  }
}
