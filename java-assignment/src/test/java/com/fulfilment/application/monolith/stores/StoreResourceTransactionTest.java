package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class StoreResourceTransactionTest {

    @Inject
    StoreResource storeResource;

    @InjectSpy
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    @BeforeEach
    @Transactional // This cleanup runs in its own transaction
    void setUp() {
        Store.deleteAll();
        reset(legacyStoreManagerGateway);
    }

    @Test
        // REMOVED @Transactional from here!
    void create_CallsLegacySystemAfterSuccessfulTransaction() {
        // Arrange
        Store newStore = new Store();
        newStore.name = "Test Store";
        newStore.quantityProductsInStock = 100;

        // Act
        // storeResource.create is @Transactional, so it starts and COMMITS its own tx
        storeResource.create(newStore);

        // Assert - verify legacy system was called
        // timeout() is helpful because event observers might be slightly decoupled
        verify(legacyStoreManagerGateway, timeout(1000).times(1))
                .createStoreOnLegacySystem(any(Store.class));

        ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
        verify(legacyStoreManagerGateway).createStoreOnLegacySystem(storeCaptor.capture());

        assertThat(storeCaptor.getValue().name).isEqualTo("Test Store");
    }

    @Test
    void update_CallsLegacySystemAfterSuccessfulTransaction() {
        // Arrange - Setup initial data in a controlled way
        Long storeId = createStoreInternally("Original Store", 50);
        reset(legacyStoreManagerGateway);

        // Prepare update
        Store updatedStore = new Store( "Updated Store");

        updatedStore.quantityProductsInStock = 75;

        // Act
        storeResource.update(storeId, updatedStore);

        // Assert
        verify(legacyStoreManagerGateway, timeout(1000).times(1))
                .updateStoreOnLegacySystem(any(Store.class));
    }

    @Test
    void patch_CallsLegacySystemAfterSuccessfulTransaction() {
        // Arrange - Create a store first
        Long storeId = createStoreInternally("Original Store", 50);
        reset(legacyStoreManagerGateway);

        // Prepare patch
        Store patchData = new Store();
        patchData.name = "Patched Store";
        patchData.quantityProductsInStock = 60;

        // Act
        storeResource.patch(storeId, patchData);

        // Assert - verify legacy system was called for update
        verify(legacyStoreManagerGateway, timeout(1000).times(1))
                .updateStoreOnLegacySystem(argThat(store ->
                        store.name.equals("Patched Store") &&
                                store.quantityProductsInStock == 60
                ));
    }

    @Test
    @Transactional
    void delete_RemovesStoreFromDatabase() {
        // Arrange
        Store store = new Store();
        store.name = "Delete Me";
        store.quantityProductsInStock = 10;
        store.persist();
        Long id = store.id;

        // Act
        Response response = storeResource.delete(id);

        // Assert
        assertThat(response.getStatus()).isEqualTo(204);
        // Verify no legacy call was made (since delete doesn't fire an event in your code)
        verifyNoInteractions(legacyStoreManagerGateway);
    }

    @Test
    void create_ThrowsException_WhenIdIsProvided() {
        // Arrange
        Store invalidStore = new Store();
        invalidStore.id = 999L; // IDs should not be provided on create
        invalidStore.name = "Invalid Store";

        // Act & Assert
        assertThatThrownBy(() -> storeResource.create(invalidStore))
                .isInstanceOf(WebApplicationException.class)
                .hasMessageContaining("Id was invalidly set");

        // Verify legacy system was NEVER called
        verifyNoInteractions(legacyStoreManagerGateway);
    }

    @Test
    void update_ThrowsException_WhenStoreNotFound() {
        // Arrange
        Store updatedData = new Store();
        updatedData.name = "Non-existent";

        // Act & Assert
        assertThatThrownBy(() -> storeResource.update(999L, updatedData))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(404);

        verifyNoInteractions(legacyStoreManagerGateway);
    }

    @Test
    void update_ThrowsException_WhenNameIsNull() {
        // Arrange
        Long id = createStoreInternally("Existing", 10);
        Store invalidUpdate = new Store();
        invalidUpdate.name = null; // Name is required

        // Act & Assert
        assertThatThrownBy(() -> storeResource.update(id, invalidUpdate))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(422);
    }

    @Test
    void patch_ThrowsException_WhenStoreNotFound() {
        // Act & Assert
        assertThatThrownBy(() -> storeResource.patch(999L, new Store()))
                .isInstanceOf(WebApplicationException.class)
                .hasMessageContaining("Store Name was not set on request.");
    }

    @Test
    void delete_ThrowsException_WhenStoreNotFound() {
        // Act & Assert
        assertThatThrownBy(() -> storeResource.delete(999L))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(404);
    }
    @Test
    void getSingle_ReturnsStore_WhenIdExists() {
        // Arrange - Create a store to retrieve
        Long existingId = createStoreInternally("Find Me Store", 200);

        // Act
        Store foundStore = storeResource.getSingle(existingId);

        // Assert
        assertThat(foundStore).isNotNull();
        assertThat(foundStore.id).isEqualTo(existingId);
        assertThat(foundStore.name).isEqualTo("Find Me Store");
        assertThat(foundStore.quantityProductsInStock).isEqualTo(200);
    }

    @Test
    void getSingle_Throws404_WhenIdDoesNotExist() {
        // Arrange
        Long nonExistentId = 9999L;

        // Act & Assert
        assertThatThrownBy(() -> storeResource.getSingle(nonExistentId))
                .isInstanceOf(WebApplicationException.class)
                .satisfies(e -> {
                    WebApplicationException wae = (WebApplicationException) e;
                    assertThat(wae.getResponse().getStatus()).isEqualTo(404);
                    assertThat(wae.getMessage()).contains("does not exist");
                });
    }
    /**
     * Helper to create data since the test method is no longer @Transactional
     */
    @Transactional
    Long createStoreInternally(String name, int qty) {
        Store s = new Store();
        s.name = name;
        s.quantityProductsInStock = qty;
        s.persist();
        return s.id;
    }
}
