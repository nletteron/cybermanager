package com.cybermanager.api.controllers.catalog;

import com.cybermanager.api.abstracts.ApiSupport;
import com.cybermanager.api.dtos.catalog.ProductRequest;
import com.cybermanager.api.dtos.catalog.ProductResponse;
import com.cybermanager.api.mappers.catalog.CatalogApiMapper;
import com.cybermanager.application.usecases.catalog.*;
import com.cybermanager.domain.queries.catalog.GetProductDetailsQuery;
import com.cybermanager.domain.queries.catalog.SearchProductsQuery;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final SearchProductsUseCase searchProductsUseCase;
    private final GetProductDetailsUseCase getProductDetailsUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ActivateProductUseCase activateProductUseCase;
    private final DeactivateProductUseCase deactivateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final JwtAccessTokenReader tokenReader;
    private final CatalogApiMapper catalogApiMapper;

    public ProductController(
            SearchProductsUseCase searchProductsUseCase,
            GetProductDetailsUseCase getProductDetailsUseCase,
            CreateProductUseCase createProductUseCase,
            UpdateProductUseCase updateProductUseCase,
            ActivateProductUseCase activateProductUseCase,
            DeactivateProductUseCase deactivateProductUseCase,
            DeleteProductUseCase deleteProductUseCase,
            JwtAccessTokenReader tokenReader,
            CatalogApiMapper catalogApiMapper
    ) {
        this.searchProductsUseCase = searchProductsUseCase;
        this.getProductDetailsUseCase = getProductDetailsUseCase;
        this.createProductUseCase = createProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.activateProductUseCase = activateProductUseCase;
        this.deactivateProductUseCase = deactivateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.tokenReader = tokenReader;
        this.catalogApiMapper = catalogApiMapper;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> search(
            @RequestParam(name = "term", required = false) String term,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "category", required = false) String category
    ) {
        return ResponseEntity.ok(searchProductsUseCase.execute(new SearchProductsQuery(term, status, category)).stream().map(view -> new ProductResponse(view.productId(), view.name(), view.price(), view.categoryId(), view.category(), view.description(), view.status())).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable("id") UUID id) {
        var view = getProductDetailsUseCase.execute(new GetProductDetailsQuery(id));
        return ResponseEntity.ok(new ProductResponse(view.productId(), view.name(), view.price(), view.categoryId(), view.category(), view.description(), view.status()));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestHeader("Authorization") String authorization, @RequestBody ProductRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = createProductUseCase.execute(catalogApiMapper.toCreateProductCommand(actor.email(), actor.roles(), request));
        return ResponseEntity.ok(new ProductResponse(view.productId(), view.name(), view.price(), view.categoryId(), view.category(), view.description(), view.status()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id, @RequestBody ProductRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = updateProductUseCase.execute(catalogApiMapper.toUpdateProductCommand(actor.email(), actor.roles(), id, request));
        return ResponseEntity.ok(new ProductResponse(view.productId(), view.name(), view.price(), view.categoryId(), view.category(), view.description(), view.status()));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ProductResponse> activate(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = activateProductUseCase.execute(catalogApiMapper.toActivateProductCommand(actor.email(), actor.roles(), id));
        return ResponseEntity.ok(new ProductResponse(view.productId(), view.name(), view.price(), view.categoryId(), view.category(), view.description(), view.status()));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ProductResponse> deactivate(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var view = deactivateProductUseCase.execute(catalogApiMapper.toDeactivateProductCommand(actor.email(), actor.roles(), id));
        return ResponseEntity.ok(new ProductResponse(view.productId(), view.name(), view.price(), view.categoryId(), view.category(), view.description(), view.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String authorization, @PathVariable("id") UUID id) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        deleteProductUseCase.execute(catalogApiMapper.toDeleteProductCommand(actor.email(), actor.roles(), id));
        return ResponseEntity.noContent().build();
    }

}

