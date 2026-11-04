package com.cybermanager.infrastructure.out.adapters.persistences.customer;

import com.cybermanager.domain.businessobjects.customer.GeneratedArchiveFile;
import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratedArchiveFileRepositoryAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private GeneratedArchiveFileRepositoryAdapter adapter;

    @Test
    void shouldSaveAndReturnMostRecentArchiveFilesFirst() {
        assertTrue(adapter.findRecent().isEmpty());
        adapter.save(GeneratedArchiveFile.create("old.pdf", "PDF", "admin", LocalDateTime.parse("2026-08-10T09:00:00")));
        adapter.save(GeneratedArchiveFile.create("new.pdf", "PDF", "admin", LocalDateTime.parse("2026-08-11T09:00:00")));

        var result = adapter.findRecent();

        assertEquals(2, result.size());
        assertEquals("new.pdf", result.get(0).fileName());
        assertEquals("old.pdf", result.get(1).fileName());
    }

}
