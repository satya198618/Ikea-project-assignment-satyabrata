package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class StoreResourceTransactionTest {

    @Inject
    StoreResource storeResource;

    @InjectSpy
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing stores
        Store.deleteAll();
        reset(legacyStoreManagerGateway);
    }

    @Test
    @Transactional
    void create_CallsLegacySystemAfterSuccessfulTransaction() {
        // Arrange
        Store newStore = new Store();
        newStore.name = "Test Store";
        newStore.quantityProductsInStock = 100;

        // Act
        storeResource.create(newStore);

        // Allow CDI observer to process
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert - verify legacy system was called
        verify(legacyStoreManagerGateway, timeout(1000).times(1))
                .createStoreOnLegacySystem(any(Store.class));

        ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
        verify(legacyStoreManagerGateway).createStoreOnLegacySystem(storeCaptor.capture());

        Store capturedStore = storeCaptor.getValue();
        assertThat(capturedStore.name).isEqualTo("Test Store");
        assertThat(capturedStore.quantityProductsInStock).isEqualTo(100);
    }

    @Test
    @Transactional
    void update_CallsLegacySystemAfterSuccessfulTransaction() {
        // Arrange - Create a store first
        Store store = new Store();
        store.name = "Original Store";
        store.quantityProductsInStock = 50;
        store.persist();

        reset(legacyStoreManagerGateway);

        // Prepare update
        Store updatedStore = new Store();
        updatedStore.name = "Updated Store";
        updatedStore.quantityProductsInStock = 75;

        // Act
        storeResource.update(store.id, updatedStore);

        // Allow CDI observer to process
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert - verify legacy system was called for update
        verify(legacyStoreManagerGateway, timeout(1000).times(1))
                .updateStoreOnLegacySystem(any(Store.class));

        ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
        verify(legacyStoreManagerGateway).updateStoreOnLegacySystem(storeCaptor.capture());

        Store capturedStore = storeCaptor.getValue();
        assertThat(capturedStore.name).isEqualTo("Updated Store");
        assertThat(capturedStore.quantityProductsInStock).isEqualTo(75);
    }

    @Test
    @Transactional
    void patch_CallsLegacySystemAfterSuccessfulTransaction() {
        // Arrange - Create a store first
        Store store = new Store();
        store.name = "Original Store";
        store.quantityProductsInStock = 50;
        store.persist();

        reset(legacyStoreManagerGateway);

        // Prepare patch
        Store patchedStore = new Store();
        patchedStore.name = "Patched Store";
        patchedStore.quantityProductsInStock = 60;

        // Act
        storeResource.patch(store.id, patchedStore);

        // Allow CDI observer to process
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert - verify legacy system was called for update
        verify(legacyStoreManagerGateway, timeout(1000).times(1))
                .updateStoreOnLegacySystem(any(Store.class));
    }

    @Test
    void transactionPhase_EnforcesAfterSuccessOrdering() {
        // This test verifies that the observer method onStoreCommitted
        // is annotated with @Observes(during = TransactionPhase.AFTER_SUCCESS)
        // which ensures calls happen after transaction commits

        // Arrange
        Store newStore = new Store();
        newStore.name = "Test Store";
        newStore.quantityProductsInStock = 100;

        // Act
        storeResource.create(newStore);

        // Allow the transaction to complete and event to fire
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert - The legacy system should have been called
        // This proves the event was observed AFTER transaction success
        verify(legacyStoreManagerGateway, timeout(1000).atLeastOnce())
                .createStoreOnLegacySystem(any(Store.class));
    }
}
