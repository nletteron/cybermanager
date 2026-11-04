package com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.sales.ConnectionPricingGridJpaEntity;
import com.cybermanager.infrastructure.out.entities.sales.ConnectionPricingTierJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionPricingJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private ConnectionPricingGridJpaRepository gridRepository;
    @Autowired
    private ConnectionPricingJpaRepository repository;

    @Test
    void shouldFindTiersByGridOrderedByDuration() {
        ConnectionPricingGridJpaEntity grid = gridRepository.save(grid());
        repository.save(tier(grid, 60));
        repository.save(tier(grid, 30));

        var result = repository.findAllByGridIdOrderByDurationMinutesAsc(grid.id);

        assertEquals(2, result.size());
        assertEquals(30, result.get(0).durationMinutes);
    }

    @Test
    void shouldRejectTierWithoutGrid() {
        ConnectionPricingTierJpaEntity entity = tier(null, 30);

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(entity));
    }

    @Test
    void shouldReturnEmptyWhenGridDoesNotExist() {

        assertTrue(repository.findAllByGridIdOrderByDurationMinutesAsc(999L).isEmpty());

    }

    private ConnectionPricingGridJpaEntity grid() {
        ConnectionPricingGridJpaEntity entity = new ConnectionPricingGridJpaEntity();
        entity.active = true;
        entity.createdAt = LocalDateTime.now();
        entity.updatedAt = LocalDateTime.now();
        return entity;
    }

    private ConnectionPricingTierJpaEntity tier(ConnectionPricingGridJpaEntity grid, int duration) {
        ConnectionPricingTierJpaEntity entity = new ConnectionPricingTierJpaEntity();
        entity.grid = grid;
        entity.durationMinutes = duration;
        entity.price = new BigDecimal("2.00");
        entity.createdAt = LocalDateTime.now();
        entity.updatedAt = LocalDateTime.now();
        return entity;
    }

}
