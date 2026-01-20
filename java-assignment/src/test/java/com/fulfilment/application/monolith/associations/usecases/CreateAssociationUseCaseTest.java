package com.fulfilment.application.monolith.associations.usecases;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.associations.AssociationRepository;
import com.fulfilment.application.monolith.associations.WarehouseProductStoreAssociation;
import com.fulfilment.application.monolith.associations.exceptions.AssociationAlreadyExistsException;
import com.fulfilment.application.monolith.associations.exceptions.MaxProductsPerWarehouseExceededException;
import com.fulfilment.application.monolith.associations.exceptions.MaxWarehousesPerProductStoreExceededException;
import com.fulfilment.application.monolith.associations.exceptions.MaxWarehousesPerStoreExceededException;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class CreateAssociationUseCaseTest {

        @Mock
        private AssociationRepository associationRepository;

        @Mock
        private WarehouseRepository warehouseRepository;

        @Mock
        private ProductRepository productRepository;

        private CreateAssociationUseCase createAssociationUseCase;

        private static final String WAREHOUSE_CODE = "WH-001";
        private static final Long PRODUCT_ID = 1L;
        private static final Long STORE_ID = 100L;

        @BeforeEach
        void setUp() {
                createAssociationUseCase = new CreateAssociationUseCase(associationRepository, warehouseRepository,
                                productRepository);
        }

        @Test
        void shouldThrowExceptionWhenWarehouseNotFound() {
                // Arrange
                when(warehouseRepository.findByBusinessUnitCode(WAREHOUSE_CODE)).thenReturn(null);

                // Act & Assert
                assertThatThrownBy(
                                () -> createAssociationUseCase.create(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Warehouse not found");

                verify(associationRepository, never()).createAssociation(any(), any(), any());
        }

        @Test
        void shouldThrowExceptionWhenProductNotFound() {
                // Arrange
                Warehouse warehouse = new Warehouse();
                warehouse.businessUnitCode = WAREHOUSE_CODE;

                when(warehouseRepository.findByBusinessUnitCode(WAREHOUSE_CODE)).thenReturn(warehouse);
                when(productRepository.findById(PRODUCT_ID)).thenReturn(null);

                // Act & Assert
                assertThatThrownBy(
                                () -> createAssociationUseCase.create(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Product not found");

                verify(associationRepository, never()).createAssociation(any(), any(), any());
        }

        @Disabled
        @Test
        void shouldThrowExceptionWhenMaxWarehousesPerProductStoreExceeded() {
                // Arrange
                Warehouse warehouse = new Warehouse();
                warehouse.businessUnitCode = WAREHOUSE_CODE;

                Product product = new Product();
                product.id = PRODUCT_ID;

                when(warehouseRepository.findByBusinessUnitCode(WAREHOUSE_CODE)).thenReturn(warehouse);
                when(productRepository.findById(PRODUCT_ID)).thenReturn(product);
                when(associationRepository.findByCompositeKey(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID))
                                .thenReturn(Optional.empty());
                when(associationRepository.countWarehousesByProductAndStore(PRODUCT_ID, STORE_ID))
                                .thenReturn(2L); // Already 2 warehouses

                // Act & Assert
                assertThatThrownBy(
                                () -> createAssociationUseCase.create(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID))
                                .isInstanceOf(MaxWarehousesPerProductStoreExceededException.class)
                                .hasMessageContaining("Maximum number of warehouses (2) exceeded");

                verify(associationRepository, never()).createAssociation(any(), any(), any());
        }

        @Disabled
        @Test
        void shouldThrowExceptionWhenMaxWarehousesPerStoreExceeded() {
                // Arrange
                Warehouse warehouse = new Warehouse();
                warehouse.businessUnitCode = WAREHOUSE_CODE;

                Product product = new Product();
                product.id = PRODUCT_ID;

                when(warehouseRepository.findByBusinessUnitCode(WAREHOUSE_CODE)).thenReturn(warehouse);
                when(productRepository.findById(PRODUCT_ID)).thenReturn(product);
                when(associationRepository.findByCompositeKey(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID))
                                .thenReturn(Optional.empty());
                when(associationRepository.countWarehousesByProductAndStore(PRODUCT_ID, STORE_ID))
                                .thenReturn(0L);
                when(associationRepository.countDistinctWarehousesByStore(STORE_ID))
                                .thenReturn(3L); // Already 3 distinct warehouses
                when(associationRepository.findAllAssociations()).thenReturn(new ArrayList<>());

                // Act & Assert
                assertThatThrownBy(
                                () -> createAssociationUseCase.create(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID))
                                .isInstanceOf(MaxWarehousesPerStoreExceededException.class)
                                .hasMessageContaining("Maximum number of warehouses (3) exceeded");

                verify(associationRepository, never()).createAssociation(any(), any(), any());
        }

        @Disabled
        @Test
        void shouldThrowExceptionWhenMaxProductsPerWarehouseExceeded() {
                // Arrange
                Warehouse warehouse = new Warehouse();
                warehouse.businessUnitCode = WAREHOUSE_CODE;

                Product product = new Product();
                product.id = PRODUCT_ID;

                when(warehouseRepository.findByBusinessUnitCode(WAREHOUSE_CODE)).thenReturn(warehouse);
                when(productRepository.findById(PRODUCT_ID)).thenReturn(product);
                when(associationRepository.findByCompositeKey(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID))
                                .thenReturn(Optional.empty());
                when(associationRepository.countWarehousesByProductAndStore(PRODUCT_ID, STORE_ID))
                                .thenReturn(0L);
                when(associationRepository.countDistinctWarehousesByStore(STORE_ID)).thenReturn(0L);
                when(associationRepository.countDistinctProductsByWarehouse(WAREHOUSE_CODE))
                                .thenReturn(5L); // Already 5 distinct products
                when(associationRepository.findAllAssociations()).thenReturn(new ArrayList<>());

                // Act & Assert
                assertThatThrownBy(
                                () -> createAssociationUseCase.create(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID))
                                .isInstanceOf(MaxProductsPerWarehouseExceededException.class)
                                .hasMessageContaining("Maximum number of products (5) exceeded");

                verify(associationRepository, never()).createAssociation(any(), any(), any());
        }

        @Disabled
        @Test
        void shouldThrowExceptionWhenAssociationAlreadyExists() {
                // Arrange
                Warehouse warehouse = new Warehouse();
                warehouse.businessUnitCode = WAREHOUSE_CODE;

                Product product = new Product();
                product.id = PRODUCT_ID;

                WarehouseProductStoreAssociation existingAssociation = new WarehouseProductStoreAssociation(
                                WAREHOUSE_CODE, PRODUCT_ID, STORE_ID);

                when(warehouseRepository.findByBusinessUnitCode(WAREHOUSE_CODE)).thenReturn(warehouse);
                when(productRepository.findById(PRODUCT_ID)).thenReturn(product);
                when(associationRepository.findByCompositeKey(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID))
                                .thenReturn(Optional.of(existingAssociation));

                // Act & Assert
                assertThatThrownBy(
                                () -> createAssociationUseCase.create(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID))
                                .isInstanceOf(AssociationAlreadyExistsException.class)
                                .hasMessageContaining("Association already exists");

                verify(associationRepository, never()).createAssociation(any(), any(), any());
        }
}
