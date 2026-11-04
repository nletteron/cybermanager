package com.cybermanager.infrastructure.out.adapters.persistences.repositories.catalog;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.catalog.ProductJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private ProductJpaRepository repository;

    @Test
    void shouldPersistAndFindProduct() {
        ProductJpaEntity product = new ProductJpaEntity();
        product.id = UUID.randomUUID();
        product.name = "Cafe";
        product.price = new BigDecimal("1.20");
        product.category = "Boissons";
        product.description = "Expresso";
        product.status = "ACTIVE";

        repository.save(product);

        assertTrue(repository.findById(product.id).isPresent());
        assertEquals("Cafe", repository.findAll().get(0).name);
    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExist() {
        assertTrue(repository.findById(UUID.randomUUID()).isEmpty());
    }

}
