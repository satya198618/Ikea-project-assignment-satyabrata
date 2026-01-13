package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.BusinessRuleViolationException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseAlreadyArchivedException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
public class ReplaceWarehouseUseCaseTest {

        @Mock
        private WarehouseStore warehouseStore;

        @Mock
        private LocationResolver locationResolver;

        @InjectMocks
        private ReplaceWarehouseUseCase replaceWarehouseUseCase;

        @Captor
        private ArgumentCaptor<Warehouse> warehouseCaptor;

        private Warehouse currentWarehouse;
        private Warehouse newWarehouse;
        private Location location;
        private static final String BUSINESS_UNIT_CODE = "MWH.TEST";
        private static final String LOCATION_ID = "AMSTERDAM-001";

        @BeforeEach
        void setUp() {
                // Setup current active warehouse
                currentWarehouse = new Warehouse();
                currentWarehouse.businessUnitCode = BUSINESS_UNIT_CODE;
                currentWarehouse.location = LOCATION_ID;
                currentWarehouse.capacity = 100;
                currentWarehouse.stock = 50;
                currentWarehouse.createdAt = LocalDateTime.now().minusDays(1);
                currentWarehouse.archivedAt = null;

                // Setup new warehouse
                newWarehouse = new Warehouse();
                newWarehouse.businessUnitCode = BUSINESS_UNIT_CODE;
                newWarehouse.location = "ROTTERDAM-001";
                newWarehouse.capacity = 200;
                newWarehouse.stock = 50;

                // Setup location
                location = new Location("ROTTERDAM-001", 10, 1000);
        }

        @Test
        void replace_SuccessfullyReplacesWarehouse() {
                // Arrange
                when(warehouseStore.findByBusinessUnitCode(BUSINESS_UNIT_CODE)).thenReturn(currentWarehouse);
                when(locationResolver.resolveByIdentifier(newWarehouse.location)).thenReturn(location);
                doNothing().when(warehouseStore).update(any(Warehouse.class));
                doNothing().when(warehouseStore).create(any(Warehouse.class));

                // Act
                replaceWarehouseUseCase.replace(newWarehouse);

                // Assert - Verify current warehouse is archived
                verify(warehouseStore).update(warehouseCaptor.capture());
                Warehouse archivedWarehouse = warehouseCaptor.getAllValues().get(0);
                assertThat(archivedWarehouse.archivedAt)
                                .as("Current warehouse should be archived")
                                .isNotNull();
                assertThat(archivedWarehouse.businessUnitCode).isEqualTo(currentWarehouse.businessUnitCode);

                // Verify new warehouse is created with correct properties
                verify(warehouseStore).create(warehouseCaptor.capture());
                Warehouse createdWarehouse = warehouseCaptor.getAllValues().get(1);
                assertThat(createdWarehouse)
                                .as("Created warehouse should have correct properties")
                                .satisfies(w -> {
                                        assertThat(w.businessUnitCode).isEqualTo(BUSINESS_UNIT_CODE);
                                        assertThat(w.capacity).isEqualTo(200);
                                        assertThat(w.stock).isEqualTo(50);
                                        assertThat(w.location).isEqualTo("ROTTERDAM-001");
                                        assertThat(w.createdAt).isNotNull();
                                        assertThat(w.archivedAt).isNull();
                                });
        }

        @Test
        void replace_ThrowsWhenCurrentWarehouseNotFound() {
                // Arrange
                when(warehouseStore.findByBusinessUnitCode(BUSINESS_UNIT_CODE))
                                .thenReturn(null);

                // Act & Assert
                assertThatThrownBy(() -> replaceWarehouseUseCase.replace(newWarehouse))
                                .isInstanceOf(WarehouseNotFoundException.class)
                                .hasMessageContaining(
                                                "Warehouse not found with business unit code: " + BUSINESS_UNIT_CODE);

                verify(warehouseStore, never()).update(any());
                verify(warehouseStore, never()).create(any());
        }

        @Test
        void replace_ThrowsWhenCurrentWarehouseAlreadyArchived() {
                // Arrange
                currentWarehouse.archivedAt = LocalDateTime.now();
                when(warehouseStore.findByBusinessUnitCode(BUSINESS_UNIT_CODE))
                                .thenReturn(currentWarehouse);

                // Act & Assert
                assertThatThrownBy(() -> replaceWarehouseUseCase.replace(newWarehouse))
                                .isInstanceOf(WarehouseAlreadyArchivedException.class)
                                .hasMessageContaining("is already archived");

                verify(warehouseStore, never()).update(any());
                verify(warehouseStore, never()).create(any());
        }

        @Test
        void replace_ThrowsWhenLocationNotResolved() {
                // Arrange
                when(warehouseStore.findByBusinessUnitCode(BUSINESS_UNIT_CODE))
                                .thenReturn(currentWarehouse);
                when(locationResolver.resolveByIdentifier(newWarehouse.location))
                                .thenThrow(new com.fulfilment.application.monolith.warehouses.domain.exceptions.LocationNotFoundException(
                                                newWarehouse.location));

                // Act & Assert
                assertThatThrownBy(() -> replaceWarehouseUseCase.replace(newWarehouse))
                                .isInstanceOf(com.fulfilment.application.monolith.warehouses.domain.exceptions.LocationNotFoundException.class)
                                .hasMessageContaining(newWarehouse.location);

                verify(warehouseStore, never()).update(any());
                verify(warehouseStore, never()).create(any());
        }

        @Test
        void replace_ThrowsWhenNewCapacityExceedsLocationMax() {
                // Arrange
                newWarehouse.capacity = 2000; // Exceeds location's max capacity of 1000
                when(warehouseStore.findByBusinessUnitCode(BUSINESS_UNIT_CODE))
                                .thenReturn(currentWarehouse);
                when(locationResolver.resolveByIdentifier(newWarehouse.location))
                                .thenReturn(location);

                // Act & Assert
                assertThatThrownBy(() -> replaceWarehouseUseCase.replace(newWarehouse))
                                .isInstanceOf(BusinessRuleViolationException.class)
                                .hasMessageContaining("exceeds location maximum capacity");

                verify(warehouseStore, never()).update(any());
                verify(warehouseStore, never()).create(any());
        }

        @Test
        void replace_ThrowsWhenNewCapacityLessThanCurrentStock() {
                // Arrange
                newWarehouse.capacity = 40; // Less than current stock of 50
                when(warehouseStore.findByBusinessUnitCode(BUSINESS_UNIT_CODE))
                                .thenReturn(currentWarehouse);
                when(locationResolver.resolveByIdentifier(newWarehouse.location))
                                .thenReturn(location);

                // Act & Assert
                assertThatThrownBy(() -> replaceWarehouseUseCase.replace(newWarehouse))
                                .isInstanceOf(BusinessRuleViolationException.class)
                                .hasMessageContaining("must be able to accommodate current warehouse stock");

                verify(warehouseStore, never()).update(any());
                verify(warehouseStore, never()).create(any());
        }

        @Test
        void replace_ThrowsWhenStockDoesNotMatch() {
                // Arrange
                newWarehouse.stock = 60; // Doesn't match current warehouse stock of 50
                when(warehouseStore.findByBusinessUnitCode(BUSINESS_UNIT_CODE)).thenReturn(currentWarehouse);
                when(locationResolver.resolveByIdentifier(newWarehouse.location)).thenReturn(location);

                // Act & Assert
                assertThatThrownBy(() -> replaceWarehouseUseCase.replace(newWarehouse))
                                .isInstanceOf(BusinessRuleViolationException.class)
                                .hasMessageContaining("must match current warehouse stock");

                verify(warehouseStore, never()).update(any());
                verify(warehouseStore, never()).create(any());
        }
}