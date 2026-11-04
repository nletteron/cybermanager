package com.cybermanager.infrastructure.out.entities.sales;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales.SaleJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaleLineJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private SaleJpaRepository sales;
    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistSaleLineEntityWithSaleRelation() {
        SaleJpaEntity sale = sale();
        sales.save(sale);
        SaleLineJpaEntity line = new SaleLineJpaEntity();
        line.sale = sale;
        line.label = "Cafe";
        line.quantity = 1;
        line.unitPrice = new BigDecimal("1.20");
        line.totalPrice = new BigDecimal("1.20");

        entityManager.persist(line);
        entityManager.flush();

        assertEquals("Cafe", entityManager.find(SaleLineJpaEntity.class, line.id).label);
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
