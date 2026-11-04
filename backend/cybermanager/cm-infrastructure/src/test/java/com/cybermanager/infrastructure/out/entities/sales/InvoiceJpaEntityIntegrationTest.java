package com.cybermanager.infrastructure.out.entities.sales;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales.InvoiceJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private InvoiceJpaRepository repository;

    @Test
    void shouldPersistInvoiceEntityAndCascadeLines() {
        InvoiceJpaEntity invoice = invoice();
        InvoiceLineJpaEntity line = new InvoiceLineJpaEntity();
        line.invoice = invoice;
        line.label = "Connexion";
        line.quantity = 1;
        line.unitPrice = new BigDecimal("4.00");
        line.totalPrice = new BigDecimal("4.00");
        invoice.lines = new ArrayList<>();
        invoice.lines.add(line);

        repository.saveAndFlush(invoice);

        InvoiceJpaEntity saved = repository.findById(invoice.id).orElseThrow();
        assertEquals("INV-001", saved.invoiceNumber);
        assertEquals(1, saved.lines.size());
        assertEquals("Connexion", saved.lines.get(0).label);
    }

    private InvoiceJpaEntity invoice() {
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.id = UUID.randomUUID();
        entity.invoiceNumber = "INV-001";
        entity.customerName = "Alice";
        entity.issuedAt = LocalDateTime.now();
        entity.status = "ISSUED";
        entity.totalAmount = new BigDecimal("4.00");
        return entity;
    }

}
