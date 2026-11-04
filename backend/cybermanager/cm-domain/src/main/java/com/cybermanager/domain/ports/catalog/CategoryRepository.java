package com.cybermanager.domain.ports.catalog;

import com.cybermanager.domain.businessobjects.catalog.Category;
import com.cybermanager.domain.valueobjects.catalog.CategoryId;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(CategoryId id);

    boolean existsByNameIgnoreCase(String name);

    List<Category> search(String term);

}
