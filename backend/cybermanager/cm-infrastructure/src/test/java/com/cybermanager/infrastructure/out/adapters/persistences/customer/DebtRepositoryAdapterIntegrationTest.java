package com.cybermanager.infrastructure.out.adapters.persistences.customer;

import com.cybermanager.domain.businessobjects.customer.DebtRecord;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.customer.DebtId;
import com.cybermanager.domain.valueobjects.customer.DebtStatus;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebtRepositoryAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private DebtRepositoryAdapter adapter;

    @Test
    void shouldSaveQueryOpenDebtsAndDeleteByCustomerIds() {
        CustomerId customerId = CustomerId.newId();
        DebtRecord openDebt = adapter.save(DebtRecord.create(customerId, "Session", Money.of("3.50"), LocalDateTime.parse("2026-08-12T10:00:00")));
        adapter.save(openDebt.settle(LocalDateTime.parse("2026-08-12T11:00:00"), "Reglee"));
        adapter.save(DebtRecord.create(customerId, "Snack", Money.of("1.20"), LocalDateTime.parse("2026-08-12T12:00:00")));

        var openDebts = adapter.findOpenDebts();
        adapter.deleteByCustomerIds(List.of(customerId));

        assertEquals(1, openDebts.size());
        assertEquals(DebtStatus.OPEN, openDebts.get(0).status());
        assertTrue(adapter.findByCustomerId(customerId).isEmpty());
    }

    @Test
    void shouldKeepIdenticalDebtsLinkedToDistinctSalesWhenOneIsDeleted() {
        CustomerId customerId = CustomerId.newId();
        UUID firstSaleId = UUID.randomUUID();
        UUID secondSaleId = UUID.randomUUID();
        LocalDateTime time = LocalDateTime.parse("2026-11-02T10:00:00");
        var first = adapter.save(DebtRecord.createForSale(customerId, "Produit 1", Money.of("2"), time, firstSaleId));
        var second = adapter.save(DebtRecord.createForSale(customerId, "Produit 1", Money.of("2"), time, secondSaleId));

        adapter.deleteById(first.id());

        var remaining = adapter.findByCustomerId(customerId);
        assertEquals(1, remaining.size());
        assertEquals(second.id(), remaining.getFirst().id());
        assertEquals(secondSaleId, remaining.getFirst().saleId());
    }

    @Test
    void shouldReturnEmptyForMissingDebt() {
        assertTrue(adapter.findById(DebtId.newId()).isEmpty());
        assertTrue(adapter.findByCustomerIds(List.of(CustomerId.newId())).isEmpty());
    }

}
