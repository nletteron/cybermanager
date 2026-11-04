package com.cybermanager.infrastructure.out.adapters.persistences.sales;

import com.cybermanager.domain.businessobjects.sales.Sale;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.sales.SaleId;
import com.cybermanager.domain.valueobjects.sales.SaleType;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaleRepositoryAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private SaleRepositoryAdapter adapter;

    @Test
    void shouldSaveFindQueryAndDeleteSalesWithLines() {
        CustomerId deletedCustomer = CustomerId.newId();
        CustomerId keptCustomer = CustomerId.newId();
        UUID sessionId = UUID.randomUUID();
        Sale deletedSale = Sale.create(deletedCustomer, sessionId, SaleType.CONNECTION_TIME, LocalDateTime.parse("2026-08-12T09:00:00"), List.of(saleLine("1h", "4.00")), Money.of("4.00"));
        Sale keptSale = Sale.create(keptCustomer, SaleType.PRODUCTS, LocalDateTime.parse("2026-08-12T10:00:00"), List.of(saleLine("Cafe", "1.20")), Money.of("1.20"));

        var saved = adapter.save(deletedSale);
        adapter.save(keptSale);
        assertEquals(2, adapter.findByDay(LocalDate.of(2026, 8, 12)).size());

        adapter.deleteByCustomerIds(List.of(deletedCustomer));

        assertTrue(adapter.findById(saved.id()).isEmpty());
        assertEquals(1, adapter.findByCustomerIds(List.of(deletedCustomer, keptCustomer)).size());
        assertEquals(keptCustomer, adapter.findByCustomerId(keptCustomer).get(0).customerId());
    }

    @Test
    void shouldReturnEmptyWhenSaleDoesNotExist() {
        assertTrue(adapter.findById(SaleId.newId()).isEmpty());
        assertTrue(adapter.findByCustomerId(CustomerId.newId()).isEmpty());
    }

}
