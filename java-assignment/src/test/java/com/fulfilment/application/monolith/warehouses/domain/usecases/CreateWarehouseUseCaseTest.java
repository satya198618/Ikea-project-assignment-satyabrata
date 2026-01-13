package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.BusinessRuleViolationException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseAlreadyExistsException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateWarehouseUseCaseTest {

        @Mock
        private WarehouseStore warehouseStore;

        @Mock
        private LocationResolver locationResolver;

        @Mock
        private WarehouseRepository warehouseRepository;

        private CreateWarehouseUseCase createWarehouseUseCase;

        private Warehouse validWarehouse;
        private Location validLocation;
        private static final String BUSINESS_UNIT_CODE = "MWH.TEST";
        private static final String LOCATION_ID = "AMSTERDAM-001";

        @BeforeEach
        void setUp() {
                // Manual constructor injection to ensure mocks are properly used
                createWarehouseUseCase = new CreateWarehouseUseCase(warehouseStore, locationResolver,
                                warehouseRepository);
                // Setup valid warehouse
                validWarehouse = new Warehouse();
                validWarehouse.businessUnitCode = BUSINESS_UNIT_CODE;
                validWarehouse.location = LOCATION_ID;
                validWarehouse.capacity = 100;
                validWarehouse.stock = 50;

                // Setup valid location
                validLocation = new Location(LOCATION_ID, 5, 1000); // max 5 warehouses, max 1000 capacity
        }

        @Test
        void create_SuccessfullyCreatesWarehouse() {
                // Arrange
                when(warehouseRepository.findByBusinessUnitCode(BUSINESS_UNIT_CODE)).thenReturn(null);
                when(locationResolver.resolveByIdentifier(LOCATION_ID)).thenReturn(validLocation);
                when(warehouseRepository.countActiveWarehousesByLocation(LOCATION_ID)).thenReturn(0L);

                // Capture the created warehouse
                Warehouse[] createdWarehouse = new Warehouse[1];
                doAnswer(invocation -> {
                        createdWarehouse[0] = invocation.getArgument(0);
                        return null;
                }).when(warehouseStore).create(any(Warehouse.class));

                // Act
                createWarehouseUseCase.create(validWarehouse);

                // Assert
                // Verify the interaction
                verify(warehouseStore).create(any(Warehouse.class));

                // Verify the created warehouse properties
                assertThat(createdWarehouse[0])
                                .as("Created warehouse should not be null")
                                .isNotNull();

                assertThat(createdWarehouse[0].businessUnitCode)
                                .as("Business unit code should match")
                                .isEqualTo(BUSINESS_UNIT_CODE);

                assertThat(createdWarehouse[0].location)
                                .as("Location should match")
                                .isEqualTo(LOCATION_ID);

                assertThat(createdWarehouse[0].capacity)
                                .as("Capacity should be set")
                                .isEqualTo(validWarehouse.capacity);

                assertThat(createdWarehouse[0].createdAt)
                                .as("Created timestamp should be set")
                                .isNotNull();

                assertThat(createdWarehouse[0].archivedAt)
                                .as("Archived timestamp should be null for new warehouse")
                                .isNull();
        }

        @Test
        void create_ThrowsWhenBusinessUnitCodeExists() {
                // Arrange
                Warehouse existingWarehouse = new Warehouse();
                existingWarehouse.businessUnitCode = BUSINESS_UNIT_CODE;
                existingWarehouse.archivedAt = null;

                when(warehouseRepository.findByBusinessUnitCode(BUSINESS_UNIT_CODE))
                                .thenReturn(existingWarehouse);

                // Act & Assert
                // Act & Assert
                assertThatThrownBy(() -> createWarehouseUseCase.create(validWarehouse))
                                .isInstanceOf(WarehouseAlreadyExistsException.class)
                                .hasMessageContaining(BUSINESS_UNIT_CODE);

                verify(warehouseStore, never()).create(any());
        }

        @Test
        void create_ThrowsWhenLocationNotResolved() {
                // Arrange
                // Arrange
                when(warehouseRepository.findByBusinessUnitCode(BUSINESS_UNIT_CODE)).thenReturn(null);
                when(locationResolver.resolveByIdentifier(LOCATION_ID))
                                .thenThrow(new LocationNotFoundException(LOCATION_ID));

                // Act & Assert
                assertThatThrownBy(() -> createWarehouseUseCase.create(validWarehouse))
                                .isInstanceOf(LocationNotFoundException.class)
                                .hasMessageContaining(LOCATION_ID);

                verify(warehouseStore, never()).create(any());
        }

        @Test
        void create_ThrowsWhenMaxWarehousesReached() {
                // Arrange
                when(warehouseRepository.findByBusinessUnitCode(BUSINESS_UNIT_CODE)).thenReturn(null);
                when(locationResolver.resolveByIdentifier(LOCATION_ID)).thenReturn(validLocation);
                when(warehouseRepository.countActiveWarehousesByLocation(LOCATION_ID))
                                .thenReturn((long) validLocation.maxNumberOfWarehouses);

                // Act & Assert
                // Act & Assert
                assertThatThrownBy(() -> createWarehouseUseCase.create(validWarehouse))
                                .isInstanceOf(BusinessRuleViolationException.class)
                                .hasMessageContaining("Maximum number of warehouses");

                verify(warehouseStore, never()).create(any());
        }

        @Test
        void create_ThrowsWhenCapacityExceedsLocationMax() {
                // Arrange
                validWarehouse.capacity = validLocation.maxCapacity + 1;

                when(warehouseRepository.findByBusinessUnitCode(BUSINESS_UNIT_CODE)).thenReturn(null);
                when(locationResolver.resolveByIdentifier(LOCATION_ID)).thenReturn(validLocation);
                when(warehouseRepository.countActiveWarehousesByLocation(LOCATION_ID)).thenReturn(0L);

                // Act & Assert
                // Act & Assert
                assertThatThrownBy(() -> createWarehouseUseCase.create(validWarehouse))
                                .isInstanceOf(BusinessRuleViolationException.class)
                                .hasMessageContaining("exceeds location maximum capacity");

                verify(warehouseStore, never()).create(any());
        }

        @Test
        void create_AllowsDuplicateBusinessUnitCodeIfArchived() {
                // Arrange
                Warehouse archivedWarehouse = new Warehouse();
                archivedWarehouse.businessUnitCode = BUSINESS_UNIT_CODE;
                archivedWarehouse.archivedAt = LocalDateTime.now().minusDays(1);

                when(warehouseRepository.findByBusinessUnitCode(BUSINESS_UNIT_CODE))
                                .thenReturn(archivedWarehouse);
                when(locationResolver.resolveByIdentifier(LOCATION_ID)).thenReturn(validLocation);
                when(warehouseRepository.countActiveWarehousesByLocation(LOCATION_ID)).thenReturn(0L);
                doNothing().when(warehouseStore).create(any(Warehouse.class));

                // Act
                createWarehouseUseCase.create(validWarehouse);

                // Assert
                verify(warehouseStore).create(validWarehouse);
        }
}