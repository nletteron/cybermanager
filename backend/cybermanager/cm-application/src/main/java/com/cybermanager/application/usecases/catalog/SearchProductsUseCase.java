package com.cybermanager.application.usecases.catalog;

import com.cybermanager.application.views.catalog.ProductView;
import com.cybermanager.domain.businessobjects.catalog.Category;
import com.cybermanager.domain.businessobjects.catalog.Product;
import com.cybermanager.domain.ports.catalog.CategoryRepository;
import com.cybermanager.domain.ports.catalog.ProductRepository;
import com.cybermanager.domain.queries.catalog.SearchProductsQuery;
import com.cybermanager.domain.valueobjects.catalog.CategoryId;
import com.cybermanager.domain.valueobjects.catalog.ProductStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SearchProductsUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public SearchProductsUseCase(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }


    @Transactional(readOnly = true)
    public List<ProductView> execute(SearchProductsQuery query) {
        ProductStatus status = query.status() == null || query.status().isBlank() ? null : ProductStatus.valueOf(query.status().toUpperCase());
        return productRepository.search(query.term(), status, query.category()).stream().map(this::toView).toList();
    }


    private ProductView toView(Product product) {
        return new ProductView(product.id().value(), product.name(), product.price().amount(), product.categoryId() == null ? null : product.categoryId().value(), product.category(), product.description(), product.status().name());
    }


    private Category requireCategory(java.util.UUID categoryId) {
        if (categoryId == null) throw new IllegalArgumentException("Category is required");
        return categoryRepository.findById(new CategoryId(categoryId)).orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

}
