package com.cybermanager.infrastructure.out.adapters.persistences.repositories.session;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.session.CafeSessionJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CafeSessionJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CafeSessionJpaRepository repository;

    @Test
    void shouldQueryAndDeleteSessionsByDateActiveAndCustomer() {
        UUID customerId = UUID.randomUUID();
        repository.save(session(customerId, LocalDateTime.parse("2026-08-12T09:00:00"), null));
        repository.save(session(UUID.randomUUID(), LocalDateTime.parse("2026-08-13T09:00:00"), LocalDateTime.parse("2026-08-13T10:00:00")));

        assertEquals(1, repository.findByEndedAtIsNull().size());
        assertEquals(1, repository.findByStartedAtBetween(LocalDateTime.parse("2026-08-12T00:00:00"), LocalDateTime.parse("2026-08-13T00:00:00")).size());
        assertEquals(1, repository.findByCustomerIdInOrderByStartedAtDesc(List.of(customerId)).size());

        repository.deleteByCustomerIdIn(List.of(customerId));
        assertTrue(repository.findByCustomerIdInOrderByStartedAtDesc(List.of(customerId)).isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenNoSessionMatches() {
        assertTrue(repository.findByEndedAtIsNull().isEmpty());
    }

    private CafeSessionJpaEntity session(UUID customerId, LocalDateTime startedAt, LocalDateTime endedAt) {
        CafeSessionJpaEntity entity = new CafeSessionJpaEntity();
        entity.id = UUID.randomUUID();
        entity.customerId = customerId;
        entity.workstation = "PC-01";
        entity.startedAt = startedAt;
        entity.endedAt = endedAt;
        entity.paid = false;
        entity.pausedMinutes = 0;
        entity.pausedSeconds = 0;
        entity.consumedMinutes = 0;
        entity.consumedSeconds = 0;
        entity.calculatedPrice = BigDecimal.ZERO;
        return entity;
    }

}
