package com.cybermanager.infrastructure.out.adapters.persistences.session;

import com.cybermanager.domain.businessobjects.session.CafeSession;
import com.cybermanager.domain.valueobjects.customer.CustomerId;
import com.cybermanager.domain.valueobjects.session.SessionId;
import com.cybermanager.domain.valueobjects.shared.Money;
import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CafeSessionRepositoryAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CafeSessionRepositoryAdapter adapter;

    @Test
    void shouldSaveFindQueryActiveDayAndDeleteSessions() {
        CustomerId deletedCustomer = CustomerId.newId();
        CustomerId keptCustomer = CustomerId.newId();
        CafeSession active = CafeSession.start(deletedCustomer, "PC-01", LocalDateTime.parse("2026-08-12T09:00:00"));
        CafeSession stopped = CafeSession.start(keptCustomer, "PC-02", LocalDateTime.parse("2026-08-12T10:00:00"))
                .stop(LocalDateTime.parse("2026-08-12T11:05:30"), Money.of("4.00"), false);

        var savedActive = adapter.save(active);
        adapter.save(stopped);
        adapter.deleteByCustomerIds(List.of(deletedCustomer));

        assertTrue(adapter.findById(savedActive.id()).isEmpty());
        assertTrue(adapter.findActive().isEmpty());
        assertEquals(1, adapter.findByDay(LocalDate.of(2026, 8, 12)).size());
        assertEquals(3930, adapter.findByCustomerIds(List.of(keptCustomer)).get(0).consumedSeconds());
    }

    @Test
    void shouldReturnEmptyWhenSessionDoesNotExist() {
        assertTrue(adapter.findById(SessionId.newId()).isEmpty());
        assertTrue(adapter.findByCustomerIds(List.of(CustomerId.newId())).isEmpty());
    }

}
