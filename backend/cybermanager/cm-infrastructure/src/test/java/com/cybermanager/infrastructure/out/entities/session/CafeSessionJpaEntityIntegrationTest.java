package com.cybermanager.infrastructure.out.entities.session;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.session.CafeSessionJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CafeSessionJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CafeSessionJpaRepository repository;

    @Test
    void shouldPersistCafeSessionEntityFields() {
        CafeSessionJpaEntity entity = new CafeSessionJpaEntity();
        entity.id = UUID.randomUUID();
        entity.customerId = UUID.randomUUID();
        entity.workstation = "PC-01";
        entity.startedAt = LocalDateTime.parse("2026-08-12T09:00:00");
        entity.endedAt = LocalDateTime.parse("2026-08-12T10:05:30");
        entity.paid = false;
        entity.pausedMinutes = 1;
        entity.pausedSeconds = 60;
        entity.consumedMinutes = 66;
        entity.consumedSeconds = 3930;
        entity.calculatedPrice = new BigDecimal("4.00");

        repository.saveAndFlush(entity);

        CafeSessionJpaEntity saved = repository.findById(entity.id).orElseThrow();
        assertEquals(3930, saved.consumedSeconds);
        assertEquals(new BigDecimal("4.00"), saved.calculatedPrice);
    }

}
