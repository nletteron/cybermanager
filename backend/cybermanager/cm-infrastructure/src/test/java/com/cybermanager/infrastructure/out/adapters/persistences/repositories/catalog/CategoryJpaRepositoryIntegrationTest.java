package com.cybermanager.infrastructure.out.adapters.persistences.repositories.catalog;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.catalog.CategoryJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CategoryJpaRepository repository;

    @Test
    void shouldPersistAndFindCategoryByNameIgnoringCase() {
        repository.save(category("Boissons"));

        assertTrue(repository.existsByNameIgnoreCase("boissons"));
    }

    @Test
    void shouldRejectDuplicateCategoryName() {
        repository.save(category("Boissons"));
        repository.save(category("Boissons"));

        assertThrows(DataIntegrityViolationException.class, () -> repository.flush());
    }

    private CategoryJpaEntity category(String name) {

        CategoryJpaEntity entity = new CategoryJpaEntity();
        entity.id = UUID.randomUUID();
        entity.name = name;
        entity.description = "Description";
        entity.vatRate = new BigDecimal("18.00");
        return entity;

    }

}
