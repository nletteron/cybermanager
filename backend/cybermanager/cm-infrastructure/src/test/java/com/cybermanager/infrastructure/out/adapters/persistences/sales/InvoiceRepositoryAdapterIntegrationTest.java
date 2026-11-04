package com.cybermanager.infrastructure.out.adapters.persistences.sales;

import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.sales.InvoiceStatus;
import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceRepositoryAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private InvoiceRepositoryAdapter adapter;

    @Test
    void shouldSaveFindAndSearchInvoicesWithLines() {
        CustomerId customerId = CustomerId.newId();
        var invoice = adapter.save(invoice("INV-2026-001", "Alice", InvoiceStatus.ISSUED, LocalDateTime.parse("2026-08-12T10:00:00"), customerId));
        adapter.save(invoice("INV-2026-002", "Bob", InvoiceStatus.CANCELLED, LocalDateTime.parse("2026-08-13T10:00:00"), customerId));

        var result = adapter.search("001", "ali", InvoiceStatus.ISSUED, LocalDate.of(2026, 8, 12), LocalDate.of(2026, 8, 12));

        assertTrue(adapter.findById(invoice.id()).isPresent());
        assertEquals(1, result.size());
        assertEquals("INV-2026-001", result.get(0).invoiceNumber());
        assertEquals("Connexion", result.get(0).lines().get(0).label());
    }

    @Test
    void shouldReturnEmptyWhenInvoiceDoesNotExistOrSearchDoesNotMatch() {
        assertTrue(adapter.findById(UUID.randomUUID()).isEmpty());
        assertTrue(adapter.search("missing", null, null, null, null).isEmpty());
    }

}
