package com.cybermanager.infrastructure.out.entities.company;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.company.CompanyProfileJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyProfileJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CompanyProfileJpaRepository repository;

    @Test
    void shouldPersistCompanyProfileFields() {
        CompanyProfileJpaEntity entity = new CompanyProfileJpaEntity();
        entity.id = UUID.randomUUID();
        entity.legalName = "Cyber Cafe";
        entity.city = "Paris";
        entity.updatedAt = LocalDateTime.parse("2026-08-12T10:00:00");

        repository.saveAndFlush(entity);

        CompanyProfileJpaEntity saved = repository.findById(entity.id).orElseThrow();
        assertEquals("Cyber Cafe", saved.legalName);
        assertEquals("Paris", saved.city);
    }

    @Test
    void shouldReturnEmptyForMissingCompanyProfileEntity() {
        assertTrue(repository.findById(UUID.randomUUID()).isEmpty());
    }

}
