package com.cybermanager.infrastructure.out.entities.customer;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.customer.DebtJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DebtJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private DebtJpaRepository repository;

    @Test
    void shouldPersistDebtEntityFields() {
        DebtJpaEntity entity = new DebtJpaEntity();
        entity.id = UUID.randomUUID();
        entity.customerId = UUID.randomUUID();
        entity.saleId = UUID.randomUUID();
        entity.label = "Session";
        entity.comment = "";
        entity.amount = new BigDecimal("3.50");
        entity.status = "OPEN";
        entity.createdAt = LocalDateTime.parse("2026-08-12T10:00:00");

        repository.saveAndFlush(entity);

        DebtJpaEntity saved = repository.findById(entity.id).orElseThrow();
        assertEquals(new BigDecimal("3.50"), saved.amount);
        assertEquals("OPEN", saved.status);
        assertEquals(entity.saleId, saved.saleId);
    }

}
