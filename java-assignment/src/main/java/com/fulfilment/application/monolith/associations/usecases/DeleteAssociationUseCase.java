package com.fulfilment.application.monolith.associations.usecases;

import com.fulfilment.application.monolith.associations.AssociationRepository;
import com.fulfilment.application.monolith.associations.exceptions.AssociationNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DeleteAssociationUseCase {

    private static final Logger LOGGER = Logger.getLogger(DeleteAssociationUseCase.class);

    private final AssociationRepository associationRepository;

    @Inject
    public DeleteAssociationUseCase(AssociationRepository associationRepository) {
        this.associationRepository = associationRepository;
    }

    @Transactional
    public void delete(String warehouseBusinessUnitCode, Long productId, Long storeId) {
        // Check if association exists
        if (associationRepository
                .findByCompositeKey(warehouseBusinessUnitCode, productId, storeId)
                .isEmpty()) {
            throw new AssociationNotFoundException(warehouseBusinessUnitCode, productId, storeId);
        }

        // Delete the association
        associationRepository.deleteAssociation(warehouseBusinessUnitCode, productId, storeId);

        LOGGER.infof(
                "Association deleted: warehouse=%s, product=%d, store=%d",
                warehouseBusinessUnitCode, productId, storeId);
    }
}
