package com.cybermanager.infrastructure.out.entities.sales;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales.ConnectionPricingGridJpaRepository;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales.ConnectionPricingJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConnectionPricingTierJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private ConnectionPricingGridJpaRepository grids;
    @Autowired
    private ConnectionPricingJpaRepository repository;

    @Test
    void shouldPersistConnectionPricingTierEntityRelation() {
        ConnectionPricingGridJpaEntity grid = new ConnectionPricingGridJpaEntity();
        grid.active = true;
        grid.createdAt = LocalDateTime.now();
        grid.updatedAt = LocalDateTime.now();
        grids.save(grid);

        ConnectionPricingTierJpaEntity tier = tier(grid);
        repository.saveAndFlush(tier);

        assertEquals(grid.id, repository.findById(tier.id).orElseThrow().grid.id);
    }

    @Test
    void shouldRejectTierWithoutGrid() {
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(tier(null)));
    }

    private ConnectionPricingTierJpaEntity tier(ConnectionPricingGridJpaEntity grid) {

        ConnectionPricingTierJpaEntity tier = new ConnectionPricingTierJpaEntity();
        tier.grid = grid;
        tier.durationMinutes = 30;
        tier.price = new BigDecimal("2.00");
        tier.createdAt = LocalDateTime.now();
        tier.updatedAt = LocalDateTime.now();
        return tier;

    }

}
