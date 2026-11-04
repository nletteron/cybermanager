package com.cybermanager.infrastructure.out.entities.sales;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.sales.InvoiceJpaRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.PropertyValueException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceLineJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private InvoiceJpaRepository invoices;
    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistInvoiceLineEntityWithRequiredInvoice() {
        InvoiceJpaEntity invoice = invoice();
        invoices.save(invoice);
        InvoiceLineJpaEntity line = line(invoice);

        entityManager.persist(line);
        entityManager.flush();

        assertEquals("Connexion", entityManager.find(InvoiceLineJpaEntity.class, line.id).label);
    }

    @Test
    void shouldRejectInvoiceLineWithoutInvoice() {

        InvoiceLineJpaEntity line = line(null);

        assertThrows(PropertyValueException.class, () -> {

            entityManager.persist(line);
            entityManager.flush();

        });

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

    private InvoiceLineJpaEntity line(InvoiceJpaEntity invoice) {
        InvoiceLineJpaEntity line = new InvoiceLineJpaEntity();
        line.invoice = invoice;
        line.label = "Connexion";
        line.quantity = 1;
        line.unitPrice = new BigDecimal("4.00");
        line.totalPrice = new BigDecimal("4.00");
        return line;
    }

}
