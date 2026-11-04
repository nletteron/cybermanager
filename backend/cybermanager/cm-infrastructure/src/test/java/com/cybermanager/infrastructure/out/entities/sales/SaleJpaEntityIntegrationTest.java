package com.cybermanager.infrastructure.out.entities.sales;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales.SaleJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaleJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private SaleJpaRepository repository;

    @Test
    void shouldPersistSaleEntityAndCascadeLines() {
        SaleJpaEntity sale = sale();
        SaleLineJpaEntity line = new SaleLineJpaEntity();
        line.sale = sale;
        line.label = "Cafe";
        line.quantity = 1;
        line.unitPrice = new BigDecimal("1.20");
        line.totalPrice = new BigDecimal("1.20");
        sale.lines = new ArrayList<>();
        sale.lines.add(line);

        repository.saveAndFlush(sale);

        SaleJpaEntity saved = repository.findById(sale.id).orElseThrow();
        assertEquals("PRODUCTS", saved.type);
        assertEquals(1, saved.lines.size());
        assertEquals("Cafe", saved.lines.get(0).label);
    }

    private SaleJpaEntity sale() {
        SaleJpaEntity entity = new SaleJpaEntity();
        entity.id = UUID.randomUUID();
        entity.customerId = UUID.randomUUID();
        entity.type = "PRODUCTS";
        entity.soldAt = LocalDateTime.now();
        entity.totalAmount = new BigDecimal("1.20");
        return entity;
    }

}
