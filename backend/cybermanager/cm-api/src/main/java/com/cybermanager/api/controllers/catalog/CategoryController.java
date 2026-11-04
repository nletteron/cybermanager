package com.cybermanager.api.controllers.catalog;

import com.cybermanager.api.abstracts.ApiSupport;
import com.cybermanager.api.dtos.catalog.CategoryRequest;
import com.cybermanager.api.dtos.catalog.CategoryResponse;
import com.cybermanager.api.mappers.catalog.CatalogApiMapper;
import com.cybermanager.application.usecases.catalog.CreateCategoryUseCase;
import com.cybermanager.application.usecases.catalog.SearchCategoriesUseCase;
import com.cybermanager.domain.queries.catalog.SearchCategoriesQuery;
import com.cybermanager.infrastructure.out.adapters.security.users.JwtAccessTokenReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final SearchCategoriesUseCase searchCategoriesUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final JwtAccessTokenReader tokenReader;
    private final CatalogApiMapper catalogApiMapper;

    public CategoryController(
            SearchCategoriesUseCase searchCategoriesUseCase,
            CreateCategoryUseCase createCategoryUseCase,
            JwtAccessTokenReader tokenReader,
            CatalogApiMapper catalogApiMapper
    ) {
        this.searchCategoriesUseCase = searchCategoriesUseCase;
        this.createCategoryUseCase = createCategoryUseCase;
        this.tokenReader = tokenReader;
        this.catalogApiMapper = catalogApiMapper;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> search(@RequestParam(required = false) String term) {
        return ResponseEntity.ok(searchCategoriesUseCase.execute(new SearchCategoriesQuery(term)).stream()
                .map(v -> new CategoryResponse(v.categoryId(), v.name(), v.description(), v.vatRate()))
                .toList());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestHeader("Authorization") String authorization, @RequestBody CategoryRequest request) {
        var actor = ApiSupport.actor(authorization, tokenReader);
        var v = createCategoryUseCase.execute(catalogApiMapper.toCreateCategoryCommand(actor.roles(), request));
        return ResponseEntity.ok(new CategoryResponse(v.categoryId(), v.name(), v.description(), v.vatRate()));
    }

}
