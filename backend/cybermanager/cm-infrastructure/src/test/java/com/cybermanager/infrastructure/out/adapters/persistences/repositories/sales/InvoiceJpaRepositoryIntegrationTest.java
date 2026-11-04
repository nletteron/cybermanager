package com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.sales.InvoiceJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private InvoiceJpaRepository repository;

    @Test
    void shouldPersistAndFindInvoice() {
        InvoiceJpaEntity entity = invoice("INV-001");

        repository.save(entity);

        assertTrue(repository.findById(entity.id).isPresent());
        assertEquals("INV-001", repository.findAll().get(0).invoiceNumber);
    }

    @Test
    void shouldReturnEmptyWhenInvoiceDoesNotExist() {
        assertTrue(repository.findById(UUID.randomUUID()).isEmpty());
    }

    private InvoiceJpaEntity invoice(String number) {
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.id = UUID.randomUUID();
        entity.invoiceNumber = number;
        entity.customerName = "Alice";
        entity.issuedAt = LocalDateTime.now();
        entity.status = "ISSUED";
        entity.totalAmount = new BigDecimal("4.00");
        return entity;
    }

}
