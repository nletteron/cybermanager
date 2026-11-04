package com.cybermanager.infrastructure.out.adapters.persistences.errors;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogJpaEntity;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogTypeJpa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorCatalogPersistenceAdapterIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private ErrorCatalogPersistenceAdapter adapter;

    @Test
    void shouldSaveUpdateAndFindErrorByDomainCode() {
        adapter.saveOrUpdate(error("CUSTOMER_NOT_FOUND", "CM-BUS-0008", "Customer not found"));

        adapter.saveOrUpdate(error("CUSTOMER_NOT_FOUND", "CM-BUS-0108", "Customer cannot be found"));

        var result = adapter.findActiveByDomainCode("CUSTOMER_NOT_FOUND");
        assertTrue(result.isPresent());
        assertEquals("CM-BUS-0108", result.orElseThrow().getErrorCode());
        assertEquals(1, adapter.findActiveErrors().size());
    }

    @Test
    void shouldReturnEmptyWhenErrorDoesNotExist() {
        assertTrue(adapter.findActiveByDomainCode("MISSING").isEmpty());
    }

    private ErrorCatalogJpaEntity error(String domainCode, String errorCode, String message) {
        ErrorCatalogJpaEntity entity = new ErrorCatalogJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setDomainCode(domainCode);
        entity.setErrorCode(errorCode);
        entity.setType(ErrorCatalogTypeJpa.BUSINESS);
        entity.setMessage(message);
        entity.setHttpStatus(404);
        entity.setBoundedContext("customer");
        entity.setActive(true);
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

}
