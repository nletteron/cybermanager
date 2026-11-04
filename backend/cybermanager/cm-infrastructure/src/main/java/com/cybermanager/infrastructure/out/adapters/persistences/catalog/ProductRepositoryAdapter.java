package com.cybermanager.infrastructure.out.adapters.persistences.catalog;

import com.cybermanager.domain.businessobjects.catalog.Product;
import com.cybermanager.domain.ports.catalog.ProductRepository;
import com.cybermanager.domain.valueobjects.catalog.CategoryId;
import com.cybermanager.domain.valueobjects.catalog.ProductId;
import com.cybermanager.domain.valueobjects.catalog.ProductStatus;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.catalog.CategoryJpaRepository;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.catalog.ProductJpaRepository;
import com.cybermanager.infrastructure.out.entities.catalog.ProductJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository repository;
    private final CategoryJpaRepository categoryRepository;

    public ProductRepositoryAdapter(ProductJpaRepository repository, CategoryJpaRepository categoryRepository) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.id = product.id().value();
        entity.name = product.name();
        entity.price = product.price().amount();
        entity.categoryReference = product.categoryId() == null ? null : categoryRepository.getReferenceById(product.categoryId().value());
        entity.category = product.category();
        entity.description = product.description();
        entity.status = product.status().name();
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<Product> findById(ProductId productId) {
        return repository.findById(productId.value()).map(this::toDomain);
    }

    @Override
    public void deleteById(ProductId productId) {
        repository.deleteById(productId.value());
    }

    @Override
    public List<Product> search(String term, ProductStatus status, String category) {
        String search = term == null ? "" : term.toLowerCase();
        return repository.findAll().stream()
                .filter(item -> search.isBlank()
                        || item.name.toLowerCase().contains(search)
                        || (item.description != null && item.description.toLowerCase().contains(search)))
                .filter(item -> status == null || item.status.equals(status.name()))
                .filter(item -> category == null || category.isBlank() || item.category.equalsIgnoreCase(category))
                .map(this::toDomain)
                .toList();
    }

    private Product toDomain(ProductJpaEntity entity) {
        return new Product(new ProductId(entity.id), entity.name, new Money(entity.price), entity.categoryReference == null ? null : new CategoryId(entity.categoryReference.id), entity.category, entity.description, ProductStatus.valueOf(entity.status));
    }

}



