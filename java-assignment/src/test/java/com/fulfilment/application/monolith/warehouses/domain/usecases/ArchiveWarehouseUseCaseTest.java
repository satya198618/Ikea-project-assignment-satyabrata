package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.InvalidWarehouseStateException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseAlreadyArchivedException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArchiveWarehouseUseCaseTest {

    @Mock
    private WarehouseStore warehouseStore;

    @InjectMocks
    private ArchiveWarehouseUseCase archiveWarehouseUseCase;

    private Warehouse activeWarehouse;
    private static final String BUSINESS_UNIT_CODE = "MWH.TEST";

    @BeforeEach
    void setUp() {
        activeWarehouse = new Warehouse();
        activeWarehouse.businessUnitCode = BUSINESS_UNIT_CODE;
        activeWarehouse.location = "AMSTERDAM-001";
        activeWarehouse.capacity = 50;
        activeWarehouse.stock = 25;
        activeWarehouse.archivedAt = null;
    }

    @Test
    void archive_SuccessfullyArchivesActiveWarehouse() {
        // Arrange
        when(warehouseStore.findByBusinessUnitCode(BUSINESS_UNIT_CODE)).thenReturn(activeWarehouse);
        doNothing().when(warehouseStore).remove(any(Warehouse.class));

        // Act
        archiveWarehouseUseCase.archive(activeWarehouse);

        // Assert
        assertThat(activeWarehouse.archivedAt)
                .as("Archived timestamp should be set")
                .isNotNull();
        verify(warehouseStore).findByBusinessUnitCode(BUSINESS_UNIT_CODE);
        verify(warehouseStore).remove(activeWarehouse);
    }

    @Test
    void archive_ThrowsWhenWarehouseNotFound() {
        // Arrange
        when(warehouseStore.findByBusinessUnitCode(BUSINESS_UNIT_CODE)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> archiveWarehouseUseCase.archive(activeWarehouse))
                .isInstanceOf(WarehouseNotFoundException.class)
                .hasMessage("Warehouse not found with business unit code: " + BUSINESS_UNIT_CODE);

        verify(warehouseStore).findByBusinessUnitCode(BUSINESS_UNIT_CODE);
        verify(warehouseStore, never()).remove(any());
    }

    @Test
    void archive_ThrowsWhenWarehouseAlreadyArchived() {
        // Arrange
        activeWarehouse.archivedAt = LocalDateTime.now();
        when(warehouseStore.findByBusinessUnitCode(BUSINESS_UNIT_CODE)).thenReturn(activeWarehouse);

        // Act & Assert
        assertThatThrownBy(() -> archiveWarehouseUseCase.archive(activeWarehouse))
                .isInstanceOf(WarehouseAlreadyArchivedException.class)
                .hasMessageContaining("is already archived");

        verify(warehouseStore).findByBusinessUnitCode(BUSINESS_UNIT_CODE);
        verify(warehouseStore, never()).remove(any());
    }

    @Test
    void archive_ThrowsWhenWarehouseIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> archiveWarehouseUseCase.archive(null))
                .isInstanceOf(InvalidWarehouseStateException.class)
                .hasMessage("Warehouse cannot be null");

        verifyNoInteractions(warehouseStore);
    }
}