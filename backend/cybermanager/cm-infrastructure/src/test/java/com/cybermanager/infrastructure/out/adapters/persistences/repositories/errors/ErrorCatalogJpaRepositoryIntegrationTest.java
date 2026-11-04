package com.cybermanager.infrastructure.out.adapters.persistences.repositories.errors;

import com.cybermanager.infrastructure.configurations.persistence.AbstractCybermanagerJpaTest;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogJpaEntity;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogTypeJpa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorCatalogJpaRepositoryIntegrationTest extends AbstractCybermanagerJpaTest {

    @Autowired
    private ErrorCatalogJpaRepository repository;

    @Test
    void shouldFindActiveErrorByDomainCode() {
        ErrorCatalogJpaEntity active = error("CUSTOMER_NOT_FOUND", "CM-BUS-0008", true);
        ErrorCatalogJpaEntity inactive = error("USER_NOT_FOUND", "CM-BUS-0003", false);
        repository.saveAndFlush(active);
        repository.saveAndFlush(inactive);

        var result = repository.findByDomainCodeAndActiveTrue("CUSTOMER_NOT_FOUND");

        assertTrue(result.isPresent());
        assertEquals("CM-BUS-0008", result.orElseThrow().getErrorCode());
        assertEquals(1, repository.findByActiveTrue().size());
    }

    @Test
    void shouldReturnEmptyWhenDomainCodeIsInactive() {
        repository.saveAndFlush(error("CUSTOMER_NOT_FOUND", "CM-BUS-0008", false));

        assertTrue(repository.findByDomainCodeAndActiveTrue("CUSTOMER_NOT_FOUND").isEmpty());
    }

    private ErrorCatalogJpaEntity error(String domainCode, String errorCode, boolean active) {
        ErrorCatalogJpaEntity entity = new ErrorCatalogJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setDomainCode(domainCode);
        entity.setErrorCode(errorCode);
        entity.setType(ErrorCatalogTypeJpa.BUSINESS);
        entity.setMessage("Customer not found");
        entity.setHttpStatus(404);
        entity.setBoundedContext("customer");
        entity.setActive(active);
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

}
