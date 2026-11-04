package com.cybermanager.application.usecases.catalog;

import com.cybermanager.application.services.shared.ActorSupport;
import com.cybermanager.application.views.catalog.CategoryView;
import com.cybermanager.domain.businessobjects.catalog.Category;
import com.cybermanager.domain.commands.catalog.CreateCategoryCommand;
import com.cybermanager.domain.ports.catalog.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateCategoryUseCase {

    private final CategoryRepository repository;

    public CreateCategoryUseCase(CategoryRepository repository) {
        this.repository = repository;
    }


    public CategoryView execute(CreateCategoryCommand command) {
        ActorSupport.requireAdmin(command.actorRoles());
        if (command.name() != null && repository.existsByNameIgnoreCase(command.name().trim())) {
            throw new IllegalArgumentException("Category name already exists");
        }
        return view(repository.save(Category.create(command.name(), command.description(), command.vatRate())));
    }


    private CategoryView view(Category category) {
        return new CategoryView(category.id().value(), category.name(), category.description(), category.vatRate());
    }

}
