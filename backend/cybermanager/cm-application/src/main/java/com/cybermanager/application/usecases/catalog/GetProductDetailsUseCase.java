package com.cybermanager.application.usecases.catalog;

import com.cybermanager.application.views.catalog.ProductView;
import com.cybermanager.domain.businessobjects.catalog.Category;
import com.cybermanager.domain.businessobjects.catalog.Product;
import com.cybermanager.domain.ports.catalog.CategoryRepository;
import com.cybermanager.domain.ports.catalog.ProductRepository;
import com.cybermanager.domain.queries.catalog.GetProductDetailsQuery;
import com.cybermanager.domain.valueobjects.catalog.CategoryId;
import com.cybermanager.domain.valueobjects.catalog.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GetProductDetailsUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public GetProductDetailsUseCase(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }


    @Transactional(readOnly = true)
    public ProductView execute(GetProductDetailsQuery query) {
        return productRepository.findById(new ProductId(query.productId()))
                .map(this::toView)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }


    private ProductView toView(Product product) {
        return new ProductView(product.id().value(), product.name(), product.price().amount(), product.categoryId() == null ? null : product.categoryId().value(), product.category(), product.description(), product.status().name());
    }


    private Category requireCategory(java.util.UUID categoryId) {
        if (categoryId == null) throw new IllegalArgumentException("Category is required");
        return categoryRepository.findById(new CategoryId(categoryId)).orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

}
