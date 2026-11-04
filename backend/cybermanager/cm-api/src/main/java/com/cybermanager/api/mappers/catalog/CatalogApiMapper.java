package com.cybermanager.api.mappers.catalog;

import com.cybermanager.api.dtos.catalog.CategoryRequest;
import com.cybermanager.api.dtos.catalog.ProductRequest;
import com.cybermanager.domain.commands.catalog.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class CatalogApiMapper {

    public CreateCategoryCommand toCreateCategoryCommand(Set<String> actorRoles, CategoryRequest request) {
        return new CreateCategoryCommand(actorRoles, request.name(), request.description(), request.vatRate());
    }

    public CreateProductCommand toCreateProductCommand(String actorEmail, Set<String> actorRoles, ProductRequest request) {
        return new CreateProductCommand(actorEmail, actorRoles, request.name(), request.price(), request.categoryId(), request.description());
    }

    public UpdateProductCommand toUpdateProductCommand(String actorEmail, Set<String> actorRoles, UUID productId, ProductRequest request) {
        return new UpdateProductCommand(actorEmail, actorRoles, productId, request.name(), request.price(), request.categoryId(), request.description());
    }

    public ActivateProductCommand toActivateProductCommand(String actorEmail, Set<String> actorRoles, UUID productId) {
        return new ActivateProductCommand(actorEmail, actorRoles, productId);
    }

    public DeactivateProductCommand toDeactivateProductCommand(String actorEmail, Set<String> actorRoles, UUID productId) {
        return new DeactivateProductCommand(actorEmail, actorRoles, productId);
    }

    public DeleteProductCommand toDeleteProductCommand(String actorEmail, Set<String> actorRoles, UUID productId) {
        return new DeleteProductCommand(actorEmail, actorRoles, productId);
    }

}
