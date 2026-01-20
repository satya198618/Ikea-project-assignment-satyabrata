package com.fulfilment.application.monolith.associations.usecases;

import com.fulfilment.application.monolith.associations.AssociationRepository;
import com.fulfilment.application.monolith.associations.WarehouseProductStoreAssociation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class GetAssociationsUseCase {

    private final AssociationRepository associationRepository;

    @Inject
    public GetAssociationsUseCase(AssociationRepository associationRepository) {
        this.associationRepository = associationRepository;
    }

    public List<WarehouseProductStoreAssociation> getAll() {
        return associationRepository.findAllAssociations();
    }
}
