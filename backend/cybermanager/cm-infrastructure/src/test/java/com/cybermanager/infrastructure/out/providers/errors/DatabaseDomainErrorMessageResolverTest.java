package com.cybermanager.infrastructure.out.providers.errors;

import com.cybermanager.application.services.shared.BusinessException;
import com.cybermanager.domain.exceptions.BusinessErrorCode;
import com.cybermanager.domain.exceptions.BusinessErrorType;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogJpaEntity;
import com.cybermanager.infrastructure.out.entities.errors.ErrorCatalogTypeJpa;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseDomainErrorMessageResolverTest {

    @Test
    void shouldResolveDomainErrorFromCatalogAndRenderParameters() {
        ErrorCatalogCache cache = mock(ErrorCatalogCache.class);
        when(cache.findByDomainCode("CUSTOMER_NOT_FOUND")).thenReturn(Optional.of(error("Customer {id} not found")));
        DatabaseDomainErrorMessageResolver resolver = new DatabaseDomainErrorMessageResolver(cache);

        var result = resolver.resolve(new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.CUSTOMER_NOT_FOUND, "fallback", Map.of("id", "42")));

        assertEquals("CUSTOMER_NOT_FOUND", result.domainCode());
        assertEquals("CM-BUS-0008", result.code());
        assertEquals("Customer 42 not found", result.message());
        assertEquals(404, result.httpStatus());
    }

    @Test
    void shouldFallbackToDomainExceptionMetadataWhenCatalogEntryIsMissing() {
        ErrorCatalogCache cache = mock(ErrorCatalogCache.class);
        when(cache.findByDomainCode("CUSTOMER_NOT_FOUND")).thenReturn(Optional.empty());
        DatabaseDomainErrorMessageResolver resolver = new DatabaseDomainErrorMessageResolver(cache);

        var result = resolver.resolve(new BusinessException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));

        assertEquals("CUSTOMER_NOT_FOUND", result.code());
        assertEquals("Customer not found", result.message());
        assertEquals(404, result.httpStatus());
    }

    private ErrorCatalogJpaEntity error(String message) {
        ErrorCatalogJpaEntity entity = new ErrorCatalogJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setDomainCode("CUSTOMER_NOT_FOUND");
        entity.setErrorCode("CM-BUS-0008");
        entity.setType(ErrorCatalogTypeJpa.BUSINESS);
        entity.setMessage(message);
        entity.setHttpStatus(404);
        entity.setBoundedContext("customer");
        entity.setActive(true);
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

}
