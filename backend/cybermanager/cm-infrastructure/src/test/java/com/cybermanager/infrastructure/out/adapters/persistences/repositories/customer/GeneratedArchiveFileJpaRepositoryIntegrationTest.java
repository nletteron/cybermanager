package com.cybermanager.infrastructure.out.adapters.persistences.repositories.customer;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.customer.GeneratedArchiveFileJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratedArchiveFileJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private GeneratedArchiveFileJpaRepository repository;

    @Test
    void shouldFindArchivesOrderedByGeneratedAtDesc() {
        repository.save(file("old.pdf", LocalDateTime.parse("2026-08-10T09:00:00")));
        repository.save(file("new.pdf", LocalDateTime.parse("2026-08-11T09:00:00")));

        assertEquals("new.pdf", repository.findAllByOrderByGeneratedAtDesc().get(0).fileName);
    }

    @Test
    void shouldReturnEmptyWhenNoArchiveExists() {
        assertTrue(repository.findAllByOrderByGeneratedAtDesc().isEmpty());
    }

    private GeneratedArchiveFileJpaEntity file(String fileName, LocalDateTime generatedAt) {
        GeneratedArchiveFileJpaEntity entity = new GeneratedArchiveFileJpaEntity();
        entity.id = UUID.randomUUID();
        entity.fileName = fileName;
        entity.fileType = "PDF";
        entity.generatedBy = "admin";
        entity.generatedAt = generatedAt;
        return entity;
    }

}
