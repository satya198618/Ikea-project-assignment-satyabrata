package com.fulfilment.application.monolith.associations;

import com.fulfilment.application.monolith.associations.exceptions.AssociationAlreadyExistsException;
import com.fulfilment.application.monolith.associations.exceptions.AssociationNotFoundException;
import com.fulfilment.application.monolith.associations.exceptions.MaxProductsPerWarehouseExceededException;
import com.fulfilment.application.monolith.associations.exceptions.MaxWarehousesPerProductStoreExceededException;
import com.fulfilment.application.monolith.associations.exceptions.MaxWarehousesPerStoreExceededException;
import com.fulfilment.application.monolith.associations.usecases.CreateAssociationUseCase;
import com.fulfilment.application.monolith.associations.usecases.DeleteAssociationUseCase;
import com.fulfilment.application.monolith.associations.usecases.GetAssociationsUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/associations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssociationResource {

    @Inject
    CreateAssociationUseCase createAssociationUseCase;

    @Inject
    DeleteAssociationUseCase deleteAssociationUseCase;

    @Inject
    GetAssociationsUseCase getAssociationsUseCase;

    @POST
    public Response createAssociation(AssociationRequest request) {
        try {
            WarehouseProductStoreAssociation association = createAssociationUseCase.create(
                    request.warehouseBusinessUnitCode, request.productId, request.storeId);
            return Response.status(Response.Status.CREATED).entity(association).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (AssociationAlreadyExistsException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (MaxWarehousesPerProductStoreExceededException
                | MaxWarehousesPerStoreExceededException
                | MaxProductsPerWarehouseExceededException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    public Response getAllAssociations() {
        List<WarehouseProductStoreAssociation> associations = getAssociationsUseCase.getAll();
        return Response.ok(associations).build();
    }

    @DELETE
    @Path("/{warehouseCode}/{productId}/{storeId}")
    public Response deleteAssociation(
            @PathParam("warehouseCode") String warehouseCode,
            @PathParam("productId") Long productId,
            @PathParam("storeId") Long storeId) {
        try {
            deleteAssociationUseCase.delete(warehouseCode, productId, storeId);
            return Response.noContent().build();
        } catch (AssociationNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    // Request DTO
    public static class AssociationRequest {
        public String warehouseBusinessUnitCode;
        public Long productId;
        public Long storeId;

        public AssociationRequest() {
        }

        public AssociationRequest(String warehouseBusinessUnitCode, Long productId, Long storeId) {
            this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
            this.productId = productId;
            this.storeId = storeId;
        }
    }

    // Error response DTO
    public static class ErrorResponse {
        public String error;

        public ErrorResponse() {
        }

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
