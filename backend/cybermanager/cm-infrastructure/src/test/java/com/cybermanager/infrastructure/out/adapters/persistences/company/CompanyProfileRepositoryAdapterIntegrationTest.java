package com.cybermanager.infrastructure.out.adapters.persistences.company;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyProfileRepositoryAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CompanyProfileRepositoryAdapter adapter;

    @Test
    void shouldSaveAndReturnLatestCurrentProfile() {
        assertTrue(adapter.findCurrent().isEmpty());
        var oldProfile = companyProfile("Cyber Cafe Old", LocalDateTime.parse("2026-08-10T09:00:00"));
        var latestProfile = companyProfile("Cyber Cafe New", LocalDateTime.parse("2026-08-11T09:00:00"));

        adapter.save(oldProfile);
        adapter.save(latestProfile);

        var current = adapter.findCurrent();
        assertTrue(current.isPresent());
        assertEquals(latestProfile.id(), current.get().id());
        assertEquals("Cyber Cafe New", current.get().legalName());
    }

}
