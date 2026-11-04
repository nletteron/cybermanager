package com.cybermanager.infrastructure.out.entities.errors;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.adapters.persistences.repositories.errors.ErrorCatalogJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErrorCatalogJpaEntityIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private ErrorCatalogJpaRepository repository;

    @Test
    void shouldPersistErrorCatalogEntity() {
        ErrorCatalogJpaEntity entity = error("CUSTOMER_NOT_FOUND", "CM-BUS-0008");

        repository.saveAndFlush(entity);

        ErrorCatalogJpaEntity result = repository.findById(entity.getId()).orElseThrow();
        assertEquals("CUSTOMER_NOT_FOUND", result.getDomainCode());
        assertEquals("CM-BUS-0008", result.getErrorCode());
        assertEquals(ErrorCatalogTypeJpa.BUSINESS, result.getType());
    }

    @Test
    void shouldRejectErrorWithoutDomainCode() {
        ErrorCatalogJpaEntity entity = error(null, "CM-BUS-0008");

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(entity));
    }

    private ErrorCatalogJpaEntity error(String domainCode, String errorCode) {
        ErrorCatalogJpaEntity entity = new ErrorCatalogJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setDomainCode(domainCode);
        entity.setErrorCode(errorCode);
        entity.setType(ErrorCatalogTypeJpa.BUSINESS);
        entity.setMessage("Customer not found");
        entity.setHttpStatus(404);
        entity.setBoundedContext("customer");
        entity.setActive(true);
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

}
