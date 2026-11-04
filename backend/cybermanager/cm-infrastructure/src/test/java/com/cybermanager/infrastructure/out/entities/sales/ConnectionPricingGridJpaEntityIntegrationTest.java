package com.cybermanager.infrastructure.out.entities.sales;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales.ConnectionPricingGridJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionPricingGridJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private ConnectionPricingGridJpaRepository repository;

    @Test
    void shouldPersistConnectionPricingGridEntityFields() {
        ConnectionPricingGridJpaEntity entity = grid(LocalDateTime.now());

        repository.saveAndFlush(entity);

        assertTrue(repository.findById(entity.id).orElseThrow().active);
    }

    @Test
    void shouldRejectGridWithoutRequiredDates() {
        ConnectionPricingGridJpaEntity entity = grid(null);

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(entity));
    }

    private ConnectionPricingGridJpaEntity grid(LocalDateTime date) {

        ConnectionPricingGridJpaEntity entity = new ConnectionPricingGridJpaEntity();
        entity.active = true;
        entity.createdAt = date;
        entity.updatedAt = date;
        return entity;

    }

}
