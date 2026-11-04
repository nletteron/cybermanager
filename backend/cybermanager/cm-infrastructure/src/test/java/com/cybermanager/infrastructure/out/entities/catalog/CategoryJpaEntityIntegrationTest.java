package com.cybermanager.infrastructure.out.entities.catalog;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.catalog.CategoryJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoryJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CategoryJpaRepository repository;

    @Test
    void shouldPersistCategoryEntityFields() {
        CategoryJpaEntity entity = category("Boissons");

        repository.saveAndFlush(entity);

        CategoryJpaEntity saved = repository.findById(entity.id).orElseThrow();
        assertEquals("Boissons", saved.name);
        assertEquals(new BigDecimal("18.00"), saved.vatRate);
    }

    @Test
    void shouldRejectCategoryWithoutRequiredName() {
        CategoryJpaEntity entity = category(null);

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(entity));
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
