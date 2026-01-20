package com.fulfilment.application.monolith.associations.usecases;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.associations.AssociationRepository;
import com.fulfilment.application.monolith.associations.WarehouseProductStoreAssociation;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GetAssociationsUseCaseTest {

    @Mock
    private AssociationRepository associationRepository;

    private GetAssociationsUseCase getAssociationsUseCase;

    @BeforeEach
    void setUp() {
        getAssociationsUseCase = new GetAssociationsUseCase(associationRepository);
    }

    @Test
    void shouldReturnAllAssociations() {
        // Arrange
        WarehouseProductStoreAssociation assoc1 = new WarehouseProductStoreAssociation("WH-001", 1L, 100L);
        WarehouseProductStoreAssociation assoc2 = new WarehouseProductStoreAssociation("WH-002", 2L, 101L);
        WarehouseProductStoreAssociation assoc3 = new WarehouseProductStoreAssociation("WH-003", 3L, 102L);

        List<WarehouseProductStoreAssociation> expectedAssociations = Arrays.asList(assoc1, assoc2, assoc3);

        when(associationRepository.findAllAssociations()).thenReturn(expectedAssociations);

        // Act
        List<WarehouseProductStoreAssociation> result = getAssociationsUseCase.getAll();

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder(assoc1, assoc2, assoc3);
        verify(associationRepository).findAllAssociations();
    }

    @Test
    void shouldReturnEmptyListWhenNoAssociations() {
        // Arrange
        when(associationRepository.findAllAssociations()).thenReturn(Arrays.asList());

        // Act
        List<WarehouseProductStoreAssociation> result = getAssociationsUseCase.getAll();

        // Assert
        assertThat(result).isEmpty();
        verify(associationRepository).findAllAssociations();
    }
}
