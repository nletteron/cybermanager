package com.cybermanager.application.usecases.catalog;

import com.cybermanager.application.services.shared.ActorSupport;
import com.cybermanager.application.views.catalog.ProductView;
import com.cybermanager.domain.businessobjects.catalog.Category;
import com.cybermanager.domain.businessobjects.catalog.Product;
import com.cybermanager.domain.commands.catalog.DeactivateProductCommand;
import com.cybermanager.domain.ports.catalog.CategoryRepository;
import com.cybermanager.domain.ports.catalog.ProductRepository;
import com.cybermanager.domain.valueobjects.catalog.CategoryId;
import com.cybermanager.domain.valueobjects.catalog.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeactivateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public DeactivateProductUseCase(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }


    public ProductView execute(DeactivateProductCommand command) {
        ActorSupport.requireAdmin(command.actorRoles());
        Product product = productRepository.findById(new ProductId(command.productId()))
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return toView(productRepository.save(product.deactivate()));
    }


    private ProductView toView(Product product) {
        return new ProductView(product.id().value(), product.name(), product.price().amount(), product.categoryId() == null ? null : product.categoryId().value(), product.category(), product.description(), product.status().name());
    }


    private Category requireCategory(java.util.UUID categoryId) {
        if (categoryId == null) throw new IllegalArgumentException("Category is required");
        return categoryRepository.findById(new CategoryId(categoryId)).orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

}
