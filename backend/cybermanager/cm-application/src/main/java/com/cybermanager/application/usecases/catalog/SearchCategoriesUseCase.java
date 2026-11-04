package com.cybermanager.application.usecases.catalog;

import com.cybermanager.application.views.catalog.CategoryView;
import com.cybermanager.domain.businessobjects.catalog.Category;
import com.cybermanager.domain.ports.catalog.CategoryRepository;
import com.cybermanager.domain.queries.catalog.SearchCategoriesQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SearchCategoriesUseCase {

    private final CategoryRepository repository;

    public SearchCategoriesUseCase(CategoryRepository repository) {
        this.repository = repository;
    }


    @Transactional(readOnly = true)
    public List<CategoryView> execute(SearchCategoriesQuery query) {
        return repository.search(query.term()).stream()
                .map(this::view)
                .toList();
    }


    private CategoryView view(Category category) {
        return new CategoryView(category.id().value(), category.name(), category.description(), category.vatRate());
    }

}
