package com.cybermanager.infrastructure.out.entities.customer;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.customer.GeneratedArchiveFileJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeneratedArchiveFileJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private GeneratedArchiveFileJpaRepository repository;

    @Test
    void shouldPersistGeneratedArchiveFileEntityFields() {
        GeneratedArchiveFileJpaEntity entity = new GeneratedArchiveFileJpaEntity();
        entity.id = UUID.randomUUID();
        entity.fileName = "archive.pdf";
        entity.fileType = "PDF";
        entity.generatedBy = "admin";
        entity.generatedAt = LocalDateTime.parse("2026-08-12T10:00:00");

        repository.saveAndFlush(entity);

        GeneratedArchiveFileJpaEntity saved = repository.findById(entity.id).orElseThrow();
        assertEquals("archive.pdf", saved.fileName);
        assertEquals("admin", saved.generatedBy);
    }

}
