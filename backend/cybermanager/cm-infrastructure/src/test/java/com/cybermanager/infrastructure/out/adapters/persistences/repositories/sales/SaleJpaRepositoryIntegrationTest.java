package com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.sales.SaleJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaleJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private SaleJpaRepository repository;

    @Test
    void shouldQueryAndDeleteSalesByDateAndCustomer() {
        UUID customerId = UUID.randomUUID();
        repository.save(sale(customerId, LocalDateTime.parse("2026-08-12T09:00:00")));
        repository.save(sale(UUID.randomUUID(), LocalDateTime.parse("2026-08-13T09:00:00")));

        assertEquals(1, repository.findBySoldAtBetween(LocalDateTime.parse("2026-08-12T00:00:00"), LocalDateTime.parse("2026-08-13T00:00:00")).size());
        assertEquals(1, repository.findByCustomerIdOrderBySoldAtDesc(customerId).size());
        assertEquals(1, repository.findByCustomerIdInOrderBySoldAtDesc(List.of(customerId)).size());

        repository.deleteByCustomerIdIn(List.of(customerId));
        assertTrue(repository.findByCustomerIdOrderBySoldAtDesc(customerId).isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenNoSaleMatches() {
        assertTrue(repository.findByCustomerIdOrderBySoldAtDesc(UUID.randomUUID()).isEmpty());
    }

    private SaleJpaEntity sale(UUID customerId, LocalDateTime soldAt) {
        SaleJpaEntity entity = new SaleJpaEntity();
        entity.id = UUID.randomUUID();
        entity.customerId = customerId;
        entity.type = "PRODUCTS";
        entity.soldAt = soldAt;
        entity.totalAmount = new BigDecimal("1.00");
        return entity;
    }

}
