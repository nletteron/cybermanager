package com.cybermanager.infrastructure.out.adapters.persistences.catalog;

import com.cybermanager.domain.businessobjects.catalog.Category;
import com.cybermanager.domain.ports.catalog.CategoryRepository;
import com.cybermanager.domain.valueobjects.catalog.CategoryId;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.catalog.CategoryJpaRepository;
import com.cybermanager.infrastructure.out.entities.catalog.CategoryJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository repository;

    public CategoryRepositoryAdapter(CategoryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Category save(Category category) {
        var entity = new CategoryJpaEntity();
        entity.id = category.id().value();
        entity.name = category.name();
        entity.description = category.description();
        entity.vatRate = category.vatRate();
        return domain(repository.save(entity));
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return repository.findById(id.value())
                .map(this::domain);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    @Override
    public List<Category> search(String term) {
        String value = term == null ? "" : term.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream().filter(item -> value.isBlank() || item.name.toLowerCase(Locale.ROOT).contains(value)
                || item.description.toLowerCase(Locale.ROOT).contains(value))
                .sorted(Comparator.comparing(item -> item.name.toLowerCase(Locale.ROOT)))
                .map(this::domain)
                .toList();
    }

    private Category domain(CategoryJpaEntity entity) {
        return new Category(new CategoryId(entity.id), entity.name, entity.description, entity.vatRate);
    }

}
