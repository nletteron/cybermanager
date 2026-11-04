package com.cybermanager.infrastructure.out.adapters.persistences.repositories.customer;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.customer.DebtJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebtJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private DebtJpaRepository repository;

    @Test
    void shouldQueryAndDeleteDebtsByCustomerAndStatus() {
        UUID customerId = UUID.randomUUID();
        repository.save(debt(customerId, "OPEN"));
        repository.save(debt(UUID.randomUUID(), "SETTLED"));

        assertEquals(1, repository.findByCustomerId(customerId).size());
        assertEquals(1, repository.findByCustomerIdIn(List.of(customerId)).size());
        assertEquals(1, repository.findByStatus("OPEN").size());

        repository.deleteByCustomerIdIn(List.of(customerId));
        assertTrue(repository.findByCustomerId(customerId).isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenNoDebtMatches() {
        assertTrue(repository.findByStatus("OPEN").isEmpty());
    }

    private DebtJpaEntity debt(UUID customerId, String status) {
        DebtJpaEntity entity = new DebtJpaEntity();
        entity.id = UUID.randomUUID();
        entity.customerId = customerId;
        entity.label = "Dette";
        entity.comment = "";
        entity.amount = new BigDecimal("1.00");
        entity.status = status;
        entity.createdAt = LocalDateTime.now();
        return entity;
    }

}
