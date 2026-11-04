package com.cybermanager.infrastructure.out.adapters.persistences.repositories.company;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.company.CompanyProfileJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyProfileJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private CompanyProfileJpaRepository repository;

    @Test
    void shouldFindLatestProfileByUpdatedAt() {
        repository.save(profile("Old", LocalDateTime.parse("2026-08-10T09:00:00")));
        repository.save(profile("New", LocalDateTime.parse("2026-08-11T09:00:00")));

        var current = repository.findTopByOrderByUpdatedAtDesc();

        assertTrue(current.isPresent());
        assertEquals("New", current.get().legalName);
    }

    @Test
    void shouldReturnEmptyWhenNoProfileExists() {
        assertTrue(repository.findTopByOrderByUpdatedAtDesc().isEmpty());
    }

    private CompanyProfileJpaEntity profile(String legalName, LocalDateTime updatedAt) {
        CompanyProfileJpaEntity entity = new CompanyProfileJpaEntity();
        entity.id = UUID.randomUUID();
        entity.legalName = legalName;
        entity.updatedAt = updatedAt;
        return entity;
    }

}
