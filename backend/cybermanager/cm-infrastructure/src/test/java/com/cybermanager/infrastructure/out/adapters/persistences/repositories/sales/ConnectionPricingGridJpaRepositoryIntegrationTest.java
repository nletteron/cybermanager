package com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.sales.ConnectionPricingGridJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionPricingGridJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private ConnectionPricingGridJpaRepository repository;

    @Test
    void shouldFindActiveGrid() {
        repository.save(grid(false));
        repository.save(grid(true));

        assertTrue(repository.findFirstByActiveTrue().isPresent());
    }

    @Test
    void shouldReturnEmptyWhenNoActiveGridExists() {
        repository.save(grid(false));

        assertTrue(repository.findFirstByActiveTrue().isEmpty());
    }

    private ConnectionPricingGridJpaEntity grid(boolean active) {
        ConnectionPricingGridJpaEntity entity = new ConnectionPricingGridJpaEntity();
        entity.active = active;
        entity.createdAt = LocalDateTime.now();
        entity.updatedAt = LocalDateTime.now();
        return entity;
    }

}
