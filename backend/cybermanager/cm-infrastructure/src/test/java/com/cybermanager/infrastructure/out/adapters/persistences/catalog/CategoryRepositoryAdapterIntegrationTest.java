package com.cybermanager.infrastructure.out.adapters.persistences.catalog;

import com.cybermanager.domain.businessobjects.catalog.Category;
import com.cybermanager.domain.ports.catalog.CategoryRepository;
import com.cybermanager.domain.valueobjects.catalog.CategoryId;
import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryRepositoryAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CategoryRepository adapter;

    @Test
    void shouldSaveFindAndSearchCategory() {
        Category category = adapter.save(Category.create("Boissons", "Boissons fraiches", new BigDecimal("18.00")));

        var found = adapter.findById(category.id());
        var result = adapter.search("boiss");

        assertTrue(found.isPresent());
        assertEquals(category.id(), found.get().id());
        assertEquals(1, result.size());
        assertEquals("Boissons", result.get(0).name());
        assertTrue(adapter.existsByNameIgnoreCase("boissons"));
    }

    @Test
    void shouldReturnEmptyWhenCategoryDoesNotExist() {
        assertTrue(adapter.findById(CategoryId.newId()).isEmpty());
        assertTrue(adapter.search("unknown").isEmpty());
    }

}
