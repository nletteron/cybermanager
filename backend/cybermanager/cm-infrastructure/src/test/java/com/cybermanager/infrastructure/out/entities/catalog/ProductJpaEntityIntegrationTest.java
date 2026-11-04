package com.cybermanager.infrastructure.out.entities.catalog;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.catalog.CategoryJpaRepository;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.catalog.ProductJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CategoryJpaRepository categories;
    @Autowired
    private ProductJpaRepository repository;

    @Test
    void shouldPersistProductEntityWithCategoryRelation() {
        CategoryJpaEntity category = new CategoryJpaEntity();
        category.id = UUID.randomUUID();
        category.name = "Snacks";
        category.description = "Petite restauration";
        category.vatRate = new BigDecimal("10.00");
        categories.save(category);

        ProductJpaEntity product = new ProductJpaEntity();
        product.id = UUID.randomUUID();
        product.name = "Chips";
        product.price = new BigDecimal("1.20");
        product.categoryReference = category;
        product.category = category.name;
        product.description = "Sachet";
        product.status = "ACTIVE";

        repository.saveAndFlush(product);

        ProductJpaEntity saved = repository.findById(product.id).orElseThrow();
        assertEquals(category.id, saved.categoryReference.id);
        assertEquals(new BigDecimal("1.20"), saved.price);
    }

}
