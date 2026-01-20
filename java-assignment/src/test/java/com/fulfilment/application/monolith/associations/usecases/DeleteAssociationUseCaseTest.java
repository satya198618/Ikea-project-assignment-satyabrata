package com.fulfilment.application.monolith.associations.usecases;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.associations.AssociationRepository;
import com.fulfilment.application.monolith.associations.WarehouseProductStoreAssociation;
import com.fulfilment.application.monolith.associations.exceptions.AssociationNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeleteAssociationUseCaseTest {

    @Mock
    private AssociationRepository associationRepository;

    private DeleteAssociationUseCase deleteAssociationUseCase;

    private static final String WAREHOUSE_CODE = "WH-001";
    private static final Long PRODUCT_ID = 1L;
    private static final Long STORE_ID = 100L;

    @BeforeEach
    void setUp() {
        deleteAssociationUseCase = new DeleteAssociationUseCase(associationRepository);
    }

    @Test
    void shouldDeleteAssociationSuccessfully() {
        // Arrange
        WarehouseProductStoreAssociation existingAssociation = new WarehouseProductStoreAssociation(WAREHOUSE_CODE,
                PRODUCT_ID, STORE_ID);

        when(associationRepository.findByCompositeKey(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID))
                .thenReturn(Optional.of(existingAssociation));
        doNothing().when(associationRepository).deleteAssociation(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID);

        // Act
        deleteAssociationUseCase.delete(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID);

        // Assert
        verify(associationRepository).findByCompositeKey(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID);
        verify(associationRepository).deleteAssociation(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID);
    }

    @Test
    void shouldThrowExceptionWhenAssociationNotFound() {
        // Arrange
        when(associationRepository.findByCompositeKey(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(
                () -> deleteAssociationUseCase.delete(WAREHOUSE_CODE, PRODUCT_ID, STORE_ID))
                .isInstanceOf(AssociationNotFoundException.class)
                .hasMessageContaining("Association not found");

        verify(associationRepository, never()).deleteAssociation(any(), any(), any());
    }
}
